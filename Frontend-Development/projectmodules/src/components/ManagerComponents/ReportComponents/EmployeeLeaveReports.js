import React, { useState, useEffect } from "react";
import { jwtDecode } from "jwt-decode";
import "bootstrap/dist/css/bootstrap.min.css"; // ✅ Added Bootstrap

// ✅ Function to retrieve token directly
const getParsedToken = () => {
  const tokenObject = localStorage.getItem("jwtToken");
  if (!tokenObject) {
    console.error("JWT token not found in localStorage.");
    return null;
  }

  try {
    const parsedToken = JSON.parse(tokenObject);
    console.log("Parsed Token Data:", parsedToken); // ✅ Debugging log
    return parsedToken.data; // ✅ Extract actual JWT token
  } catch (error) {
    console.error("Error parsing JWT token:", error);
    return null;
  }
};
const EmployeeLeaveReports = () => {
  const [employeeId, setEmployeeId] = useState(""); // ✅ Holds selected employee ID
  const [employeeIds, setEmployeeIds] = useState([]); // ✅ List of employees
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [leaveType, setLeaveType] = useState("");
  const [leaveData, setLeaveData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [employees, setEmployees] = useState([]);

  // ✅ Fetch employee list from backend using Manager ID
  useEffect(() => {
        const fetchEmployees = async () => {
          try {
            const token = getParsedToken(); // ✅ Get parsed token
            if (!token) {
              alert("Missing authentication token. Please log in.");
              throw new Error("Authentication token not found.");
            }
    
            const decodedToken = jwtDecode(token);
            const managerId = decodedToken?.employeeId || null; // ✅ Extract Manager ID safely
    
            if (!managerId) {
              alert("Invalid token: Manager ID not found.");
              throw new Error("Manager ID missing in JWT payload.");
            }
    
            console.log("Extracted Manager ID:", managerId);
    
            const response = await fetch(`http://localhost:8099/employee/${managerId}/reports/getAllNamesAndIds`, {
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
    
    // Convert the object into an array of key-value pairs
    const employeeList = Object.entries(data).map(([id, name]) => ({
      id: parseInt(id, 10), // Convert the key to an integer
      name,
    }));
            setEmployeeIds(employeeList);
          } catch (error) {
            console.error("Error fetching employee data:", error);
            alert("Failed to load employee data. Please try again later.");
          }
        };
    
        fetchEmployees();
      }, []);

  useEffect(() => {
    setLeaveData([]); // ✅ Clear leave data when leaveType changes
  }, [leaveType]);

  const handleSubmit = async () => {
    if (!employeeId || !startDate || !endDate) {
      alert("Please select an employee and fill out all fields.");
      return;
    }

    try {
      setLoading(true);
      const token = getParsedToken(); // ✅ Get stored token

      console.log("Fetching leave reports for Employee ID:", employeeId);

      const response = await fetch(
        `http://localhost:8099/employee/${employeeId}/reports/leave?startDate=${startDate}&endDate=${endDate}`,
        {
          method: "GET",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
        }
      );

      if (!response.ok) {
        const errorMessage = await response.text();
        alert(`Error fetching leave reports: ${errorMessage}`);
        return;
      }

      const data = await response.json();
      console.log("Leave Reports:", data);

      const filteredData =
        leaveType === "ALL_LEAVES" || leaveType === ""
          ? data
          : data.filter((leave) => leave.leaveType === leaveType);

      setLeaveData(filteredData);
    } catch (error) {
      console.error("Error fetching leave reports:", error);
      alert("Failed to fetch leave reports.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container mt-5 p-4 bg-light rounded shadow-lg text-center" style={{ maxWidth: "850px" }}>
      <h2 className="text-primary fw-bold mb-4">Employee Leave Reports</h2>

      <div className="d-flex justify-content-center gap-4 mb-4">
        <div>
          <label className="fw-bold">Employee:</label>
          <select
            className="form-select"
            value={employeeId}
            onChange={(e) => setEmployeeId(e.target.value)}
          >
            <option value="">Select Employee</option>
            {employeeIds.map((employee) => (
              <option key={employee.id} value={employee.id}>
                {employee.name} ({employee.id})
              </option>
            ))}
          </select>
        </div>
        <div>
          <label className="fw-bold">Start Date:</label>
          <input type="date" className="form-control" value={startDate} onChange={(e) => setStartDate(e.target.value)} />
        </div>
        <div>
          <label className="fw-bold">End Date:</label>
          <input type="date" className="form-control" value={endDate} onChange={(e) => setEndDate(e.target.value)} />
        </div>
        <div>
          <label className="fw-bold">Leave Type:</label>
          <select className="form-select" value={leaveType} onChange={(e) => setLeaveType(e.target.value)}>
            <option value="">Select Leave Type</option>
            <option value="SICK_LEAVE">Sick Leave</option>
            <option value="CASUAL_LEAVE">Casual Leave</option>
            <option value="PAID_LEAVE">Paid Leave</option>
            <option value="ALL_LEAVES">All Leaves</option>
          </select>
        </div>
      </div>

      <button className="btn btn-success fw-bold px-5 py-3 rounded" onClick={handleSubmit}>Submit</button>

      {loading && <p className="text-warning fw-bold mt-4">Loading...</p>}

      {leaveData.length > 0 ? (
        <div className="table-responsive mt-4">
          <table className="table table-bordered">
            <thead className="table-dark">
              <tr>
                <th>Start Date</th>
                <th>End Date</th>
                <th>Leave Type</th>
                <th>Employee ID</th>
              </tr>
            </thead>
            <tbody>
              {leaveData.map((leave, index) => (
                <tr key={index}>
                  <td>{leave.startDate}</td>
                  <td>{leave.endDate}</td>
                  <td>{leave.leaveType}</td>
                  <td>{leave.employeeId}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        !loading && <p className="text-muted fw-bold mt-4">No data found</p>
      )}
    </div>
  );
};

export default EmployeeLeaveReports;
