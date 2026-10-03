package rut.iuct.auditoriums.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rut.iuct.auditoriums.model.Department;
import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    List<Department> findAllByOrderByNameAscIdAsc();
}
