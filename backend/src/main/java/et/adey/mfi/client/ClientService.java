package et.adey.mfi.client;

import et.adey.mfi.audit.AuditAction;
import et.adey.mfi.audit.AuditService;
import et.adey.mfi.common.BadRequestException;
import et.adey.mfi.common.FineractDates;
import et.adey.mfi.common.PageResponse;
import et.adey.mfi.fineract.FineractApiException;
import et.adey.mfi.fineract.FineractClient;
import et.adey.mfi.fineract.model.FineractClientData;
import et.adey.mfi.fineract.model.FineractCommandResult;
import et.adey.mfi.fineract.model.FineractPage;
import et.adey.mfi.fineract.model.FineractStaff;
import et.adey.mfi.loan.LoanGuard;
import et.adey.mfi.security.AccessPolicy;
import et.adey.mfi.security.Officer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class ClientService {

    private final FineractClient fineract;
    private final AccessPolicy access;
    private final LoanGuard guard;
    private final GenderCodeValues genders;
    private final AuditService audit;

    public ClientService(FineractClient fineract, AccessPolicy access, LoanGuard guard,
                         GenderCodeValues genders, AuditService audit) {
        this.fineract = fineract;
        this.access = access;
        this.guard = guard;
        this.genders = genders;
        this.audit = audit;
    }

    /**
     * page is 0-based; Fineract wants offset = page * size. The office/staff filter comes from the
     * token (AccessPolicy.clientScope), so an officer cannot list someone else's clients.
     */
    public PageResponse<ClientSummary> search(Officer officer, String search, int page, int size) {
        String name = StringUtils.hasText(search) ? search.trim() : null;
        AccessPolicy.ClientScope scope = access.clientScope(officer);
        FineractPage<FineractClientData> result =
                fineract.searchClients(name, scope.officeId(), scope.staffId(), page * size, size);
        return PageResponse.of(result.itemsOrEmpty(), ClientService::toSummary,
                page, size, result.totalFilteredRecords());
    }

    public ClientDetails getClient(Officer officer, long clientId) {
        return toDetails(guard.requireClient(officer, clientId));
    }

    // ---- Onboarding ----------------------------------------------------------------------

    /**
     * Creates a client in the caller's office. An officer creates clients for themselves; a manager
     * must choose one of their office's loan officers (staffId). Activated immediately unless
     * activate=false (an inactive client cannot get a loan in Fineract).
     */
    public ClientDetails create(Officer officer, CreateClientRequest req) {
        return audit.record(officer, AuditAction.CLIENT_CREATE, rec -> {
            long staffId = assignedOfficer(officer, req.staffId());
            ClientRules.requireAdult(req.dateOfBirth());

            String today = FineractDates.format(FineractDates.today());
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("officeId", officer.officeId());
            body.put("staffId", staffId);
            body.put("legalFormId", 1); // 1 = person (Fineract code)
            body.put("firstname", req.firstname().trim());
            body.put("lastname", req.lastname().trim());
            body.put("mobileNo", req.mobileNo());
            body.put("genderId", genders.idOf(req.gender()));
            body.put("dateOfBirth", FineractDates.format(req.dateOfBirth()));
            if (StringUtils.hasText(req.externalId())) {
                body.put("externalId", req.externalId().trim());
            }
            boolean activate = req.activate() == null || req.activate();
            body.put("active", activate);
            if (activate) {
                body.put("activationDate", today);
            }
            body.put("submittedOnDate", today);
            body.put("dateFormat", FineractDates.DATE_FORMAT);
            body.put("locale", FineractDates.LOCALE);

            FineractCommandResult result = fineract.createClient(body);
            long clientId = result.resourceId();
            rec.clientId(clientId);
            log.info("Client {} created by {} (staff {})", clientId, officer.username(), staffId);
            return toDetails(fineract.getClient(clientId));
        });
    }

    /** Edits basic details. Same scoping as reading: an officer only their own clients, a manager their office. */
    public ClientDetails update(Officer officer, long clientId, UpdateClientRequest req) {
        return audit.record(officer, AuditAction.CLIENT_UPDATE, rec -> {
            rec.clientId(clientId);
            guard.requireClient(officer, clientId);
            if (req.isEmpty()) {
                throw new BadRequestException("body", "Send at least one field to change.");
            }
            ClientRules.requireAdult(req.dateOfBirth());

            Map<String, Object> body = new LinkedHashMap<>();
            if (req.firstname() != null) body.put("firstname", req.firstname().trim());
            if (req.lastname() != null) body.put("lastname", req.lastname().trim());
            if (req.mobileNo() != null) body.put("mobileNo", req.mobileNo());
            if (req.gender() != null) body.put("genderId", genders.idOf(req.gender()));
            if (req.externalId() != null) body.put("externalId", req.externalId().trim());
            if (req.dateOfBirth() != null) {
                body.put("dateOfBirth", FineractDates.format(req.dateOfBirth()));
                body.put("dateFormat", FineractDates.DATE_FORMAT);
                body.put("locale", FineractDates.LOCALE);
            }
            fineract.updateClient(clientId, body);
            return toDetails(fineract.getClient(clientId));
        });
    }

    /**
     * Who the new client is assigned to. Officers: always themselves (sending someone else's
     * staffId is refused). Managers: must name an active loan officer of their own office.
     */
    private long assignedOfficer(Officer officer, Long requestedStaffId) {
        if (!officer.isManager()) {
            if (requestedStaffId != null && requestedStaffId != officer.staffId()) {
                throw new AccessDeniedException("Officers can only create clients assigned to themselves.");
            }
            return officer.staffId();
        }
        if (requestedStaffId == null) {
            throw new BadRequestException("staffId", "Choose the loan officer (staffId) for this client.");
        }
        FineractStaff staff;
        try {
            staff = fineract.getStaff(requestedStaffId);
        } catch (FineractApiException e) {
            throw new BadRequestException("staffId", "Staff " + requestedStaffId + " does not exist.");
        }
        if (!Objects.equals(staff.officeId(), officer.officeId())) {
            throw new AccessDeniedException("Staff " + requestedStaffId + " belongs to another office.");
        }
        if (!Boolean.TRUE.equals(staff.isLoanOfficer()) || !Boolean.TRUE.equals(staff.isActive())) {
            throw new BadRequestException("staffId", "Staff " + requestedStaffId + " is not an active loan officer.");
        }
        return requestedStaffId;
    }

    // ---- Mapping -------------------------------------------------------------------------

    static ClientSummary toSummary(FineractClientData c) {
        return new ClientSummary(c.id(), c.displayName(), c.accountNo(), c.officeName(),
                clean(c.staffName()), Boolean.TRUE.equals(c.active()), FineractDates.fromArray(c.activationDate()));
    }

    static ClientDetails toDetails(FineractClientData c) {
        return new ClientDetails(
                c.id(),
                c.accountNo(),
                c.externalId(),
                c.displayName(),
                c.firstname(),
                c.middlename(),
                c.lastname(),
                c.mobileNo(),
                FineractDates.fromArray(c.dateOfBirth()),
                c.gender() == null ? null : c.gender().label(),
                c.status() == null ? null : c.status().value(),
                Boolean.TRUE.equals(c.active()),
                c.timeline() == null ? null : FineractDates.fromArray(c.timeline().submittedOnDate()),
                FineractDates.fromArray(c.activationDate()),
                c.officeId(),
                c.officeName(),
                c.staffId(),
                clean(c.staffName()));
    }

    /** Fineract keeps stray spaces from data entry ("Derese, Samuel "). */
    private static String clean(String s) {
        return s == null ? null : s.trim();
    }
}
