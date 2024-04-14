export enum Role {
    ROLE_USER, ROLE_ADMIN, ROLE_REVIEWER
}

export namespace Role {
    export function getAsString(role: Role): string {
        switch (role) {
            case Role.ROLE_USER: return 'User';
            case Role.ROLE_ADMIN: return 'Admin';
            case Role.ROLE_REVIEWER: return 'Reviewer';
            default: return 'No role';
        }
    }
}

export function getAllRole(): Role[] {
    let roles = [];
    roles.push(Role.ROLE_USER);
    roles.push(Role.ROLE_ADMIN);
    roles.push(Role.ROLE_REVIEWER);
    return roles;
}