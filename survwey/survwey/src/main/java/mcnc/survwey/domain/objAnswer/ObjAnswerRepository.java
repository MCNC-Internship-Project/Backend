package mcnc.survwey.domain.objAnswer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ObjAnswerRepository extends JpaRepository<ObjAnswer, Long> {
}
