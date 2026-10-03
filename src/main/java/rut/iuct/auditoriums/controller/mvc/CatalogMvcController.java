package rut.iuct.auditoriums.controller.mvc;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import rut.iuct.auditoriums.dto.*;
import rut.iuct.auditoriums.service.*;

@Controller
public class CatalogMvcController {
    private final DepartmentService departments;
    private final AdditionalEquipmentService equipment;
    public CatalogMvcController(DepartmentService departments, AdditionalEquipmentService equipment) {
        this.departments = departments;
        this.equipment = equipment;
    }

    private void context(boolean department, Model model) {
        model.addAttribute("section", "catalogs");
        model.addAttribute("departmentCatalog", department);
        model.addAttribute("title", department ? "Кафедры" : "Дополнительное оборудование");
        model.addAttribute("basePath", department ? "/departments" : "/additional-equipment");
    }

    @GetMapping({"/departments", "/additional-equipment"})
    public String list(jakarta.servlet.http.HttpServletRequest request, Model model) {
        boolean department = request.getRequestURI().equals("/departments");
        context(department, model);
        model.addAttribute("items", department ? departments.list() : equipment.list());
        return "catalog/list";
    }

    @GetMapping({"/departments/new", "/additional-equipment/new"})
    public String create(jakarta.servlet.http.HttpServletRequest request, Model model) {
        boolean department = request.getRequestURI().startsWith("/departments/");
        context(department, model);
        model.addAttribute("form", department ? new DepartmentRequest() : new AdditionalEquipmentRequest());
        return "catalog/form";
    }

    @GetMapping({"/departments/{id}/edit", "/additional-equipment/{id}/edit"})
    public String edit(@PathVariable Long id, jakarta.servlet.http.HttpServletRequest request, Model model) {
        boolean department = request.getRequestURI().startsWith("/departments/");
        context(department, model);
        model.addAttribute("form", department ? departments.get(id) : equipment.get(id));
        model.addAttribute("editingId", id);
        return "catalog/form";
    }
}
