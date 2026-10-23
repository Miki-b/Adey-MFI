package et.adey.mfi.loan;

import jakarta.validation.constraints.Size;

public record UndoApprovalRequest(@Size(max = 1000) String note) {
}
