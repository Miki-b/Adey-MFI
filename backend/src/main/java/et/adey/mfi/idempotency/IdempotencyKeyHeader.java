package et.adey.mfi.idempotency;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Documents the optional Idempotency-Key header the same way on every endpoint that supports it. */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Parameter(description = "Optional. A unique id (e.g. a UUID) the app creates once per action and sends again "
        + "on every retry. A retry with the same key and body returns the first response instead of running twice. "
        + "Valid for 24 hours.",
        schema = @Schema(type = "string", example = "3f0a9c5e-6f1b-4b8e-9d2a-7c1e0b5a4d33"))
public @interface IdempotencyKeyHeader {
}
