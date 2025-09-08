package ma.gap.dtos;

import java.util.List;

public class EmployeeDTO {
    private List<Long> empId;

    public List<Long> getEmpId() {
        return empId;
    }

    public void setEmpId(List<Long> empId) {
        this.empId = empId;
    }

    public EmployeeDTO(List<Long> empId) {
        this.empId = empId;
    }

    public EmployeeDTO() {
    }
}
