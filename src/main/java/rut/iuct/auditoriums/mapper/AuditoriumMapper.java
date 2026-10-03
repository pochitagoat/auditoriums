package rut.iuct.auditoriums.mapper;

import org.springframework.stereotype.Component;
import rut.iuct.auditoriums.model.Auditorium;
import rut.iuct.auditoriums.dto.*;
import java.util.List;

@Component
public class AuditoriumMapper {
    private final DepartmentMapper departmentMapper;

    public AuditoriumMapper(DepartmentMapper departmentMapper) {
        this.departmentMapper = departmentMapper;
    }

    public AuditoriumListItem toListItem(Auditorium source) {
        AuditoriumListItem target = new AuditoriumListItem();
        copyListFields(source, target);
        return target;
    }

    private void copyListFields(Auditorium source, AuditoriumListItem target) {
        AuditMapper.copy(source, source.getId(), target);
        target.setName(source.getName());
        target.setBuilding(source.getBuilding());
        target.setType(source.getType());
        target.setFloor(source.getFloor());
        target.setArea(source.getArea());
        target.setCapacityStudents(source.getCapacityStudents());
        target.setBoardType(source.getBoardType());
        target.setInternet(source.getInternet());
        target.setHasAc(source.getHasAc());
        target.setInSchedule(source.getInSchedule());
        target.setHasActiveProjector(source.getHasActiveProjector());
        target.setHasActivePrinter(source.getHasActivePrinter());
        target.setDepartment(departmentMapper.toResponse(source.getDepartment()));
        target.setActiveWorkstationCount(source.getPcCountAuto());
    }

    public AuditoriumResponse toResponse(Auditorium source, List<AdditionalEquipmentResponse> selectedTypes) {
        AuditoriumResponse target = new AuditoriumResponse();
        copyListFields(source, target);
        target.setDescription(source.getDescription());
        target.setCapacityStudentsExtra(source.getCapacityStudentsExtra());
        target.setCapacityStaff(source.getCapacityStaff());
        target.setTotalCapacityStudents(source.getTotalCapacityStudents());
        target.setScreenSize(source.getScreenSize());
        target.setSubnet(source.getSubnet());
        target.setEquipmentIssue(source.getEquipmentIssue());
        target.setLegacyHasProjector(source.getHasProjector());
        target.setLegacyPcCountManual(source.getPcCountManual());
        boolean projectorMismatch = source.getHasProjector() != null
                && source.getHasProjector() != source.getHasActiveProjector();
        boolean pcMismatch = source.getPcCountManual() != null
                && source.getPcCountManual().longValue() != source.getPcCountAuto();
        if (projectorMismatch || pcMismatch) {
            target.setLegacyEquipmentWarning("Старые сведения об оборудовании отличаются от действующих записей");
        }
        target.setAdditionalEquipment(selectedTypes);
        return target;
    }

    public void apply(AuditoriumRequest source, Auditorium target) {
        target.setName(source.getName());
        target.setType(source.getType());
        target.setDescription(source.getDescription());
        target.setCapacityStudents(source.getCapacityStudents());
        target.setCapacityStudentsExtra(source.getCapacityStudentsExtra());
        target.setCapacityStaff(source.getCapacityStaff());
        target.setBuilding(source.getBuilding());
        target.setFloor(source.getFloor());
        target.setArea(source.getArea());
        target.setScreenSize(source.getScreenSize());
        target.setHasAc(source.getHasAc());
        target.setInternet(source.getInternet());
        target.setSubnet(source.getSubnet());
        target.setInSchedule(source.getInSchedule());
        target.setEquipmentIssue(source.getEquipmentIssue());
        target.setBoardType(source.getBoardType());
    }
}
