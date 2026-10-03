package rut.iuct.auditoriums.controller.mvc;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import rut.iuct.auditoriums.dto.*;
import rut.iuct.auditoriums.model.AuditoriumType;
import rut.iuct.auditoriums.model.BoardType;
import rut.iuct.auditoriums.service.*;

@Controller
public class AuditoriumMvcController {
    private final AuditoriumSearchService search;
    private final AuditoriumService auditoriums;
    private final DepartmentService departments;
    private final WorkstationService workstations;
    private final ProjectorService projectors;
    private final PrinterService printers;
    private final AdditionalEquipmentService equipment;
    private final PhotoService photos;
    private final NavigationSupport navigation;

    public AuditoriumMvcController(AuditoriumSearchService search, AuditoriumService auditoriums, DepartmentService departments,
            WorkstationService workstations, ProjectorService projectors, PrinterService printers,
            AdditionalEquipmentService equipment, PhotoService photos, NavigationSupport navigation) {
        this.search = search;
        this.auditoriums = auditoriums;
        this.departments = departments;
        this.workstations = workstations;
        this.projectors = projectors;
        this.printers = printers;
        this.equipment = equipment;
        this.photos = photos;
        this.navigation = navigation;
    }

    @ModelAttribute
    public void common(Model model) {
        model.addAttribute("types", AuditoriumType.values());
        model.addAttribute("boards", BoardType.values());
        model.addAttribute("section", "catalog");
    }

    @GetMapping({"/", "/auditoriums"})
    public String list(@Valid @ModelAttribute("filter") AuditoriumFilter filter, HttpServletRequest request, Model model) {
        PageResponse<AuditoriumListItem> page = search.search(filter);
        model.addAttribute("page", page);
        model.addAttribute("departments", departments.list());
        String listUrl = navigation.currentList(request);
        model.addAttribute("listUrl", listUrl);
        model.addAttribute("previousUrl", navigation.page(listUrl, Math.max(0, page.getPage() - 1)));
        model.addAttribute("nextUrl", navigation.page(listUrl, page.getPage() + 1));
        return "auditoriums/list";
    }

    @GetMapping("/auditoriums/new")
    public String create(@RequestParam(required = false) String returnTo, Model model) {
        model.addAttribute("form", new AuditoriumRequest());
        formContext(returnTo, model);
        return "auditoriums/form";
    }

    @GetMapping("/auditoriums/{id}/edit")
    public String edit(@PathVariable Long id, @RequestParam(required = false) String returnTo, Model model) {
        model.addAttribute("form", auditoriums.get(id));
        model.addAttribute("editingId", id);
        formContext(returnTo, model);
        return "auditoriums/form";
    }

    private void formContext(String returnTo, Model model) {
        String safeReturn = navigation.safeReturn(returnTo);
        model.addAttribute("returnTo", safeReturn);
        Long editingId = (Long) model.getAttribute("editingId");
        model.addAttribute("backUrl", editingId == null ? safeReturn : navigation.card(editingId, safeReturn));
        model.addAttribute("departments", departments.list());
    }

    @GetMapping("/auditoriums/{id}")
    public String card(@PathVariable Long id, @RequestParam(required = false) String returnTo,
            @RequestParam(defaultValue = "false") boolean includeWrittenOff, Model model) {
        model.addAttribute("auditorium", auditoriums.get(id));
        model.addAttribute("workstations", workstations.list(id, includeWrittenOff));
        model.addAttribute("projectors", projectors.list(id, includeWrittenOff));
        model.addAttribute("printers", printers.list(id, includeWrittenOff));
        model.addAttribute("equipmentTypes", equipment.list());
        model.addAttribute("photos", photos.list(id));
        model.addAttribute("includeWrittenOff", includeWrittenOff);
        model.addAttribute("returnTo", navigation.safeReturn(returnTo));
        return "auditoriums/card";
    }
}
