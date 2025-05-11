import React, { useState } from "react";
import { Bar, Pie } from "react-chartjs-2";
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
import "./AttendanceReports.css";

ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend, ArcElement);

const AttendanceReports = () => {
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [attendanceData, setAttendanceData] = useState([]);
  const [loading, setLoading] = useState(false);

  // ✅ Function to parse token from localStorage
  const getParsedToken = () => {
    const tokenObject = localStorage.getItem("jwtToken");
    if (!tokenObject) return null;

    try {
      const parsedToken = JSON.parse(tokenObject);
      console.log("Parsed Token:", parsedToken); // ✅ Debugging log to check parsed token
      return parsedToken.data; // ✅ Extract only the actual JWT token
    } catch (error) {
      console.error("Error parsing JWT token:", error);
      return null;
    }
  };

  // ✅ Extract Employee ID from localStorage
  const employeeId = (() => {
    const user = JSON.parse(localStorage.getItem("userLoggedIn"));
    if (user && user.employeeId) {
      return user.employeeId;
    } else {
      alert("Employee ID not found. Please log in again.");
      throw new Error("Employee ID not found in localStorage.");
    }
  })();

  const handleSubmit = async () => {
    if (!startDate || !endDate) {
      alert("Please select both Start Date and End Date.");
      return;
    }

    try {
      setLoading(true);
      setAttendanceData([]);

      const token = getParsedToken(); // ✅ Get parsed token
      if (!token) {
        alert("Missing authentication token. Please log in.");
        throw new Error("Authentication token not found.");
      }

      const response = await fetch(
        `http://localhost:8099/employee/${employeeId}/reports/attendance?startDate=${startDate}&endDate=${endDate}`,
        {
          method: "GET",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`, // ✅ Correct token format
          },
        }
      );

      if (!response.ok) {
        const errorMessage = await response.text();
        alert(`Error: ${errorMessage}`);
        return;
      }

      const data = await response.json();
      console.log("Attendance Reports:", data); // ✅ Debugging log
      setAttendanceData(data);
    } catch (error) {
      console.error("Error fetching attendance reports:", error);
      alert("An error occurred while fetching attendance reports. Please try again later.");
    } finally {
      setLoading(false);
    }
  };

  // ✅ Filter out "ON_LEAVE" entries
  const filteredAttendanceData = attendanceData.filter((attendance) => attendance.status !== "ON_LEAVE");

  // ✅ Prepare Bar Chart Data
  const barChartData = {
    labels: filteredAttendanceData.map((attendance) => new Date(attendance.date).toLocaleDateString()),
    datasets: [
      {
        label: "Work Hours",
        data: filteredAttendanceData.map((attendance) => attendance.workHours),
        backgroundColor: "rgba(54, 162, 235, 0.7)",
        borderColor: "rgba(54, 162, 235, 1)",
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
        labels: { font: { size: 14 } },
      },
      tooltip: { enabled: true },
    },
  };

  // ✅ Prepare Pie Chart Data
  const statusCounts = filteredAttendanceData.reduce(
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
        backgroundColor: ["#4caf50", "#f44336"],
        borderColor: ["#388e3c", "#d32f2f"],
        borderWidth: 1,
      },
    ],
  };

  const pieChartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: "top",
        labels: { font: { size: 14 } },
      },
      tooltip: { enabled: true },
    },
  };

  return (
    <div className="attendance-container">
      <h1 className="attendance-title">Attendance Reports</h1>
      <div className="filter-container">
        <label className="filter-label">Start Date:</label>
        <input type="date" value={startDate} onChange={(e) => setStartDate(e.target.value)} className="filter-input" />

        <label className="filter-label">End Date:</label>
        <input type="date" value={endDate} onChange={(e) => setEndDate(e.target.value)} className="filter-input" />

        <button onClick={handleSubmit} className="filter-button">
          Submit
        </button>
      </div>

      {loading && <p className="loading-text">Loading...</p>}

      {filteredAttendanceData.length > 0 ? (
        <table className="attendance-table">
          <thead>
            <tr>
              <th>Date</th>
              <th>Clock In Time</th>
              <th>Clock Out Time</th>
              <th>Status</th>
              <th>Work Hours</th>
            </tr>
          </thead>
          <tbody>
            {filteredAttendanceData.map((attendance, index) => (
              <tr key={index}>
                <td>{new Date(attendance.date).toLocaleDateString()}</td>
                  <td>{attendance.clockInTime ? new Date(attendance.clockInTime).toLocaleTimeString() : "-"}</td>
                  <td>{attendance.clockOutTime ? new Date(attendance.clockOutTime).toLocaleTimeString() : "-"}</td>
                <td>{attendance.status}</td>
                <td>{attendance.workHours}</td>
              </tr>
            ))}
          </tbody>
        </table>
      ) : (
        !loading && <p className="no-data-text">No data found</p>
      )}

      {/* ✅ Analytics Section */}
      {filteredAttendanceData.length > 0 && (
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
      )}
    </div>
  );
};

export default AttendanceReports;
