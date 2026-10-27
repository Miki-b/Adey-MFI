package et.adey.mfi.config;

import et.adey.mfi.common.ErrorCodes;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.jspecify.annotations.Nullable;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.util.ClassUtils;

import java.lang.reflect.RecordComponent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fine-tunes the generated OpenAPI spec (backend/openapi.json), which the Flutter app's Dart client is
 * generated from:
 *
 * 1. REQUIRED = NON-NULL. springdoc can't know which fields are always sent, so every field came out
 *    optional and the generated Dart models were all nullable. Rule: a component of one of our
 *    response records is required unless it is annotated JSpecify {@code @Nullable}. Primitives
 *    (int, long, boolean) are never null. Request records keep their Bean Validation rules
 *    (@NotNull/@NotBlank), because there "required" means "the client must send it".
 * 2. MONEY: every BigDecimal is {@code type: number, format: decimal}. We keep JSON numbers (strings
 *    would break every existing client and Postman); "decimal" tells clients "exact decimal, don't
 *    treat as a float". The app parses it into Decimal (see app/lib/core/formatting/money.dart).
 * 3. ERRORS: every operation documents 4XX/5XX as an RFC 7807 Problem with our stable `code` (an
 *    enum of ErrorCodes.ALL).
 */
@Configuration
public class OpenApiConfig {

    static final String BASE_PACKAGE = "et.adey.mfi";
    static final String PROBLEM = "Problem";

    @Bean
    public OpenApiCustomizer adeyOpenApiCustomizer() {
        Map<String, Class<?>> records = recordsBySchemaName();
        return openApi -> {
            Components components = openApi.getComponents();
            @SuppressWarnings("rawtypes")
            Map<String, Schema> schemas = components.getSchemas();
            if (schemas != null) {
                schemas.forEach((name, schema) -> {
                    Class<?> type = recordFor(name, records);
                    if (type != null) {
                        applyRecordRules(type, schema);
                    }
                });
            }
            components.addSchemas(PROBLEM, problemSchema());
            if (openApi.getPaths() != null) {
                openApi.getPaths().values().forEach(path -> path.readOperations().forEach(op -> {
                    op.getResponses().computeIfAbsent("4XX", k -> problemResponse(
                            "Request refused: see `code` (validation, auth, access, business rule)"));
                    op.getResponses().computeIfAbsent("5XX", k -> problemResponse(
                            "Server-side failure: see `code` (INTERNAL_ERROR, FINERACT_UNAVAILABLE, ...)"));
                }));
            }
        };
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    static void applyRecordRules(Class<?> type, Schema schema) {
        Map<String, Schema> properties = schema.getProperties();
        if (properties == null) {
            return;
        }
        boolean request = type.getSimpleName().endsWith("Request");
        List<String> required = new ArrayList<>(schema.getRequired() == null ? List.of() : schema.getRequired());
        for (RecordComponent c : type.getRecordComponents()) {
            Schema property = properties.get(c.getName());
            if (property == null) {
                continue;
            }
            if (c.getType() == BigDecimal.class) {
                property.setFormat("decimal");
            }
            if (!request && !isNullable(c) && !required.contains(c.getName())) {
                required.add(c.getName());
            }
        }
        schema.setRequired(required.isEmpty() ? null : required);
    }

    static boolean isNullable(RecordComponent c) {
        // JSpecify's @Nullable is a TYPE_USE annotation: it sits on the component's type.
        return c.getAnnotatedType().isAnnotationPresent(Nullable.class) || c.isAnnotationPresent(Nullable.class);
    }

    /** Schema names are simple class names, or @Schema(name = ...) when one is set. */
    static Map<String, Class<?>> recordsBySchemaName() {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false) {
            @Override
            protected boolean isCandidateComponent(
                    org.springframework.beans.factory.annotation.AnnotatedBeanDefinition definition) {
                return definition.getMetadata().isIndependent();
            }
        };
        scanner.addIncludeFilter(new AssignableTypeFilter(Record.class));
        Map<String, Class<?>> byName = new HashMap<>();
        for (BeanDefinition d : scanner.findCandidateComponents(BASE_PACKAGE)) {
            Class<?> type = ClassUtils.resolveClassName(d.getBeanClassName(), OpenApiConfig.class.getClassLoader());
            io.swagger.v3.oas.annotations.media.Schema named =
                    type.getAnnotation(io.swagger.v3.oas.annotations.media.Schema.class);
            String name = named != null && !named.name().isBlank() ? named.name() : type.getSimpleName();
            byName.putIfAbsent(name, type);
        }
        return byName;
    }

    /** "PageResponseClientSummary" is the generic PageResponse<ClientSummary>: match by prefix. */
    static @Nullable Class<?> recordFor(String schemaName, Map<String, Class<?>> records) {
        Class<?> exact = records.get(schemaName);
        if (exact != null) {
            return exact;
        }
        return records.entrySet().stream()
                .filter(e -> e.getValue().getTypeParameters().length > 0 && schemaName.startsWith(e.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    @SuppressWarnings("rawtypes")
    static Schema problemSchema() {
        Schema<Object> error = new Schema<>();
        error.type("object").addType("object");
        error.addProperty("field", stringSchema("The request field (or parameter) that is wrong"));
        error.addProperty("message", stringSchema("What is wrong with it"));
        error.setRequired(List.of("field", "message"));

        Schema<String> code = stringSchema("Stable error code: switch on this, not on detail");
        code.setEnum(new ArrayList<>(ErrorCodes.ALL));

        Schema<Integer> status = new Schema<>();
        status.type("integer").addType("integer");
        status.format("int32");

        ArraySchema errors = new ArraySchema();
        errors.addType("array");
        errors.setItems(error);
        errors.setDescription("Field-level problems (validation errors only)");

        Schema<Object> problem = new Schema<>();
        problem.type("object").addType("object");
        problem.setDescription("RFC 7807 problem detail, plus our stable `code` (see backend/README.md \"Errors\").");
        problem.addProperty("type", stringSchema("Problem type URI"));
        problem.addProperty("title", stringSchema("Short summary of the HTTP status"));
        problem.addProperty("status", status);
        problem.addProperty("detail", stringSchema("Human-readable explanation (may change; for people, not code)"));
        problem.addProperty("instance", stringSchema("The request path"));
        problem.addProperty("code", code);
        problem.addProperty("errors", errors);
        problem.setRequired(List.of("status", "code"));
        return problem;
    }

    static Schema<String> stringSchema(String description) {
        Schema<String> s = new Schema<>();
        s.type("string").addType("string");
        s.setDescription(description);
        return s;
    }

    static ApiResponse problemResponse(String description) {
        Schema<Object> ref = new Schema<>();
        ref.set$ref("#/components/schemas/" + PROBLEM);
        return new ApiResponse()
                .description(description)
                .content(new Content().addMediaType("application/problem+json", new MediaType().schema(ref)));
    }
}
