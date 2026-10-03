package rut.iuct.auditoriums.controller.mvc;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PlaceholderMvcController {
    @GetMapping("/requests")
    public String requests(Model model) {
        model.addAttribute("section", "requests");
        model.addAttribute("title", "Заявки");
        model.addAttribute("description", "Здесь появятся заявки о проблемах в аудиториях, их статусы и исполнители.");
        return "placeholder";
    }

    @GetMapping("/statistics")
    public String statistics(Model model) {
        model.addAttribute("section", "statistics");
        model.addAttribute("title", "Статистика и управление");
        model.addAttribute("description", "Здесь появятся графики по аудиториям, оборудованию и заявкам.");
        return "placeholder";
    }
}
