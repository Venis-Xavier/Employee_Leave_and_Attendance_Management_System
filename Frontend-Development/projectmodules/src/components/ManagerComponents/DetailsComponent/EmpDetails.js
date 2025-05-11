import React, { useState, useEffect } from "react";
import { jwtDecode } from "jwt-decode";
import "./EmpDetails.css";

const EmployeeDetails = () => {
  const [employees, setEmployees] = useState([]); // Full list of employees
  const [filteredEmployees, setFilteredEmployees] = useState([]); // Filtered list
  const [searchId, setSearchId] = useState(""); // Search input value
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // ✅ Fetch employee details connected to a manager
  useEffect(() => {
    const fetchEmployeesUnderManager = async () => {
      try {
        setLoading(true);
        setError(null);

        // ✅ Get token from localStorage
        const tokenObject = localStorage.getItem("jwtToken");
        if (!tokenObject) {
          alert("Missing authentication token. Please log in.");
          throw new Error("Authentication token not found.");
        }

        const parsedToken = JSON.parse(tokenObject); // ✅ Convert token to JSON object
        const token = parsedToken.data; // ✅ Extract only the actual JWT token

        const decodedToken = jwtDecode(token);
        const managerId = decodedToken.employeeId; // ✅ Extract Manager ID

        if (!managerId) {
          alert("Invalid token: Manager ID not found.");
          throw new Error("Manager ID missing in JWT payload.");
        }

        console.log("Extracted Manager ID:", managerId);

        const response = await fetch(`http://localhost:8092/employee/employeeDetailsUnderManager/${managerId}`, {
          method: "GET",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`, // ✅ Correct token format
          },
        });

        if (!response.ok) {
          throw new Error(`Error fetching employees: ${response.status}`);
        }

        const data = await response.json();
        console.log("Employees Under Manager Response:", data);

        setEmployees(data);
        setFilteredEmployees(data); // ✅ Initialize filtered list with full data
      } catch (error) {
        console.error("Error fetching employee details:", error);
        setError("Failed to load employee details. Please try again later.");
      } finally {
        setLoading(false);
      }
    };

    fetchEmployeesUnderManager();
  }, []);

  // ✅ Handle search input change
  const handleSearchChange = (event) => {
    const value = event.target.value;
    setSearchId(value);
  
    // ✅ Convert employeeId to string before checking
    if (value.trim() === "") {
      setFilteredEmployees(employees); // Show full list when input is empty
    } else {
      const filtered = employees.filter((employee) =>
        String(employee.employeeId).startsWith(value) // Ensure it's treated as a string
      );
      setFilteredEmployees(filtered);
    }
  };

  return (
    <div className="employee-details-container">
      <h2>Employee Details</h2>

      {/* ✅ Search Input Field */}
      <div className="filter-container">
        <label>Search Employee ID:</label>
        <input
          type="text"
          value={searchId}
          onChange={handleSearchChange}
          placeholder="Enter Employee ID"
        />
      </div>

      {loading && <p>Loading...</p>}
      {error && <p className="error">{error}</p>}

      {filteredEmployees.length > 0 ? (
        <table className="employee-table">
          <thead>
            <tr>
              <th>Employee ID</th>
              <th>First Name</th>
              <th>Last Name</th>
              <th>Email ID</th>
              <th>Department</th>
            </tr>
          </thead>
          <tbody>
            {filteredEmployees.map((employee) => (
              <tr key={employee.employeeId}>
                <td>{employee.employeeId}</td>
                <td>{employee.firstName}</td>
                <td>{employee.lastName}</td>
                <td>{employee.email}</td>
                <td>{employee.department}</td>
              </tr>
            ))}
          </tbody>
        </table>
      ) : (
        !loading && <p>No employees found matching the search.</p>
      )}
    </div>
  );
};

export default EmployeeDetails;
