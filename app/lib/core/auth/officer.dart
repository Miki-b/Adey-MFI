import 'package:adey_api/adey_api.dart';

enum Role { officer, manager }

/// The signed-in user, from GET /api/auth/me.
class Officer {
  const Officer({
    required this.userId,
    required this.username,
    required this.displayName,
    required this.officeId,
    required this.officeName,
    required this.roles,
    this.staffId,
  });

  factory Officer.fromApi(OfficerResponse r) => Officer(
        userId: r.userId,
        username: r.username,
        displayName: r.displayName.isEmpty ? r.username : r.displayName,
        staffId: r.staffId,
        officeId: r.officeId,
        officeName: r.officeName,
        roles: {
          for (final role in r.roles)
            if (role == OfficerResponseRolesEnum.MANAGER)
              Role.manager
            else if (role == OfficerResponseRolesEnum.OFFICER)
              Role.officer,
        },
      );

  final int userId;
  final String username;
  final String displayName;
  final int? staffId;
  final int officeId;
  final String officeName;
  final Set<Role> roles;

  /// UI only: hides manager screens. The backend enforces the real rule on every call.
  bool get isManager => roles.contains(Role.manager);
}
