package rut.iuct.auditoriums.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import rut.iuct.auditoriums.model.Auditorium;
import java.util.Optional;

public interface AuditoriumRepository extends JpaRepository<Auditorium, Long>, JpaSpecificationExecutor<Auditorium> {
    @Override
    @EntityGraph(attributePaths = "department")
    Page<Auditorium> findAll(Specification<Auditorium> specification, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "department")
    Optional<Auditorium> findById(Long id);

    boolean existsByDepartment_Id(Long departmentId);
}
