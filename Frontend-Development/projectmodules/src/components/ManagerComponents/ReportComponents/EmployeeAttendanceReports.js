import React, { useState, useEffect } from "react";
import { jwtDecode } from "jwt-decode";
import { Bar, Pie } from "react-chartjs-2"; // Import Bar and Pie charts
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend,
  ArcElement,
} from "chart.js";
import "./EmployeeAttendanceReports.css";
 
// Register Chart.js components
ChartJS.register(
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend,
  ArcElement
);
const getParsedToken = () => {
  const tokenObject = localStorage.getItem("jwtToken");
  if (!tokenObject) {
      console.error("JWT token not found in localStorage.");
      return null;
  }

  try {
      const parsedToken = JSON.parse(tokenObject);
      console.log("Parsed Token Object:", parsedToken);
      return parsedToken?.data || null;  // ✅ Ensure correct extraction
  } catch (error) {
      console.error("Error parsing JWT token:", error);
      return null;
  }
};
const EmployeeAttendanceReports = () => {
  const [employeeId, setEmployeeId] = useState("");
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [attendanceData, setAttendanceData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [employees, setEmployees] = useState([]);
 
  // Fetch employee IDs and names from the backend
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
          console.log(decodedToken);
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
          setEmployees(employeeList);
        } catch (error) {
          console.error("Error fetching employee data:", error);
          alert("Failed to load employee data. Please try again later.");
        }
      };
  
      fetchEmployees();
    }, []);
 
  const handleSubmit = async () => {
    if (!employeeId || !startDate || !endDate) {
      alert("Please fill out all fields before submitting.");
      return;
    }
 
    try {
      console.log(employeeId);
   //   console.log(managerId);
      setLoading(true); // Show loading spinner
      const token = getParsedToken();
      if (!token) {
        alert("Authentication token missing. Please log in.");
        return;
      }
      const response = await fetch(
        `http://localhost:8099/employee/${employeeId}/reports/attendance?startDate=${startDate}&endDate=${endDate}`,
        {
          method: "GET",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`, // Add the Bearer token here
          },
          mode: "cors",
        }
      );
      console.log(response);
      if (!response.ok) {
        const errorMessage = await response.text();
        alert(`Error: ${errorMessage}`);
        return;
      }
 
      const data = await response.json();
      console.log("Attendance Data:", data); // Debugging log
      setAttendanceData(data); // Update state with the fetched attendance data
    } catch (error) {
      console.error("Error fetching attendance data:", error);
      alert("An error occurred while fetching attendance data. Please try again later.");
    } finally {
      setLoading(false); // Hide loading spinner
    }
  };
 
  // Prepare data for Bar Chart
  const barChartData = {
    labels: attendanceData.map((attendance) =>
      new Date(attendance.date).toLocaleDateString()
    ),
    datasets: [
      {
        label: "Work Hours",
        data: attendanceData.map((attendance) => attendance.workHours),
        backgroundColor: "rgba(54, 162, 235, 0.7)", // Vibrant blue for bars
        borderColor: "rgba(54, 162, 235, 1)", // Darker blue for borders
        borderWidth: 1,
      },
    ],
  };
 
  // Prepare data for Pie Chart
  const statusCounts = attendanceData.reduce(
    (acc, attendance) => {
      acc[attendance.status] = (acc[attendance.status] || 0) + 1;
      return acc;
    },
    { PRESENT: 0, ABSENT: 0 }
  );
 
  const pieChartData = {
    labels: ["PRESENT", "ABSENT"],
    datasets: [
      {
        label: "Attendance Status",
        data: [statusCounts.PRESENT, statusCounts.ABSENT],
        backgroundColor: ["#4caf50", "#f44336"], // Green for PRESENT, Red for ABSENT
        borderColor: ["#388e3c", "#d32f2f"], // Darker shades for borders
        borderWidth: 1,
      },
    ],
  };
 
  const barChartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: "top",
        labels: {
          font: {
            size: 14,
          },
        },
      },
      tooltip: {
        enabled: true,
      },
    },
  };
 
  const pieChartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: "top",
        labels: {
          font: {
            size: 14,
          },
        },
      },
      tooltip: {
        enabled: true,
      },
    },
  };
 
  return (
    <div className="attendance-container">
      <h1>Employee Attendance Reports</h1>
      <div className="filter-container">
        <div>
          <label>Employee:</label>
          <select
            value={employeeId}
            onChange={(e) => setEmployeeId(e.target.value)}
          >
            <option value="">Select Employee</option>
            {employees.map((employee) => (
              <option key={employee.id} value={employee.id}>
                {employee.name} ({employee.id})
              </option>
            ))}
          </select>
        </div>
        <div>
          <label>Start Date:</label>
          <input
            type="date"
            value={startDate}
            onChange={(e) => setStartDate(e.target.value)}
          />
        </div>
        <div>
          <label>End Date:</label>
          <input
            type="date"
            value={endDate}
            onChange={(e) => setEndDate(e.target.value)}
          />
        </div>
        <button onClick={handleSubmit}>Submit</button>
      </div>
 
      {loading && <p>Loading...</p>}
 
      {attendanceData.length > 0 ? (
        <>
          <table className="attendance-table">
            <thead>
              <tr>
                <th>Attendance Date</th>
                <th>Clock In Time</th>
                <th>Clock Out Time</th>
                <th>Work Hours</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {attendanceData.map((attendance, index) => (
                <tr key={index}>
                  <td>{new Date(attendance.date).toLocaleDateString()}</td>
                  <td>{attendance.clockInTime ? new Date(attendance.clockInTime).toLocaleTimeString() : "-"}</td>
                  <td>{attendance.clockOutTime ? new Date(attendance.clockOutTime).toLocaleTimeString() : "-"}</td>
                  <td>{attendance.workHours}</td>
                  <td>{attendance.status}</td>
                </tr>
              ))}
            </tbody>
          </table>
 
          {/* Analytics Section */}
          <div className="analytics-container">
            <h2 className="analytics-title">Analytics</h2>
            <div className="chart-container">
              <div className="chart-item">
                <h3 className="chart-title">Work Hours by Date</h3>
                <Bar data={barChartData} options={barChartOptions} />
              </div>
              <div className="chart-item">
                <h3 className="chart-title">Attendance Status Distribution</h3>
                <Pie data={pieChartData} options={pieChartOptions} />
              </div>
            </div>
          </div>
        </>
      ) : (
        !loading && <p>No data found</p>
      )}
    </div>
  );
};
 
export default EmployeeAttendanceReports;