package task;

import engine.Target;
import engine.dto.DTOTaskOnTarget;
import engine.dto.Dto;


public interface Task {

    Dto run(Target currentTarget) throws InterruptedException;

    //DTOTaskOnTarget run(Target currentTarget);
}
