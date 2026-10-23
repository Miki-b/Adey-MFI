package et.adey.mfi.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(@NotBlank @Size(max = 100) String username,
                           @NotBlank @Size(max = 100) String password) {

    /** Never print the password, even in logs or error messages. */
    @Override
    public String toString() {
        return "LoginRequest[username=%s, password=***]".formatted(username);
    }
}
