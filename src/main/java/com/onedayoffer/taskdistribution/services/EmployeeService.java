package com.onedayoffer.taskdistribution.services;

import com.onedayoffer.taskdistribution.DTO.EmployeeDTO;
import com.onedayoffer.taskdistribution.DTO.TaskDTO;
import com.onedayoffer.taskdistribution.DTO.TaskStatus;
import com.onedayoffer.taskdistribution.repositories.EmployeeRepository;
import com.onedayoffer.taskdistribution.repositories.TaskRepository;
import com.onedayoffer.taskdistribution.repositories.entities.Employee;
import com.onedayoffer.taskdistribution.repositories.entities.Task;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.modelmapper.internal.bytebuddy.description.method.MethodDescription;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@AllArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final TaskRepository taskRepository;
    private final ModelMapper modelMapper;

    public List<EmployeeDTO> getEmployees(@Nullable String sortDirection) {
        List<Employee> employees;
        if (sortDirection != null) {
            Sort.Direction direction = Sort.Direction.valueOf(sortDirection);
            employees = employeeRepository.findAllAndSort(Sort.by(direction, "fio"));
        } else {
            employees = employeeRepository.findAll();
        }
        Type listType = new TypeToken<List<EmployeeDTO>>() {
        }.getType();
        return modelMapper.map(employees, listType);
    }

    @Transactional
    public EmployeeDTO getOneEmployee(Integer id) {
        Type listType = new TypeToken<List<TaskDTO>>() {
        }.getType();
        Employee employee = Optional.of(employeeRepository.findById(id)).get().orElse(null);
        if (Objects.nonNull(employee)) {
            return EmployeeDTO.builder()
                    .fio(employee.getFio())
                    .jobTitle(employee.getJobTitle())
                    .tasks(modelMapper.map(employee.getTasks(), listType))
                    .build();
        }
        return null;
    }

    public List<TaskDTO> getTasksByEmployeeId(Integer id) {
        Type listType = new TypeToken<List<TaskDTO>>() {
        }.getType();
        return modelMapper.map(taskRepository.getTaskByEmployeeId(id), listType);
    }

    @Transactional
    public void changeTaskStatus(Integer taskId, TaskStatus status) {
        taskRepository.getReferenceById(taskId).setStatus(status);
    }

    @Transactional
    public void postNewTask(Integer employeeId, TaskDTO newTask) {
        Task result = modelMapper.map(newTask, Task.class);
        result.setEmployee(employeeRepository.findById(employeeId).get());
        taskRepository.save(result);
    }
}