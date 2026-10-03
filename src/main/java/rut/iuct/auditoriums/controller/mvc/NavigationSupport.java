package rut.iuct.auditoriums.controller.mvc;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import java.net.URI;
import java.util.Set;

@Component("navigation")
public class NavigationSupport {
    private static final Set<String> FILTERS = Set.of("q", "type", "building", "floor", "hasProjector", "hasPrinter",
            "hasWorkstations", "internet", "hasAc", "boardType", "departmentId", "page", "size");

    public String safeReturn(String value) {
        if (value == null || value.contains("\\") || value.chars().anyMatch(Character::isISOControl)) return "/auditoriums";
        try {
            URI uri = URI.create(value);
            if (uri.isAbsolute() || uri.getRawAuthority() != null || uri.getRawFragment() != null
                    || !"/auditoriums".equals(uri.getRawPath())) return "/auditoriums";
            return value;
        } catch (IllegalArgumentException exception) { return "/auditoriums"; }
    }

    public String currentList(HttpServletRequest request) {
        var builder = UriComponentsBuilder.fromPath("/auditoriums");
        request.getParameterMap().forEach((key, values) -> {
            if (FILTERS.contains(key) && values.length > 0 && !values[0].isBlank()) builder.queryParam(key, values[0]);
        });
        return builder.build().encode().toUriString();
    }

    public String page(String listUrl, int page) {
        return UriComponentsBuilder.fromUriString(safeReturn(listUrl)).replaceQueryParam("page", page).build().toUriString();
    }

    public String card(Long id, String returnTo) {
        return UriComponentsBuilder.fromPath("/auditoriums/" + id).queryParam("returnTo", safeReturn(returnTo)).build().encode().toUriString();
    }
}
