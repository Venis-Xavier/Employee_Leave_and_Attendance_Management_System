import React, { useEffect, useState } from "react";
import { BrowserRouter as Router, Routes, Route, Navigate, Link } from "react-router-dom";
import { jwtDecode } from "jwt-decode";
import LoginPage from "./components/LoginPage"; 
import EmployeeDashboard from "./components/EmployeeComponents/EmployeeDashboard"; 
import LeaveDashboard from "./components/EmployeeComponents/LeaveComponents/LeaveDashboard"; 
import ApplyLeave from "./components/EmployeeComponents/LeaveComponents/ApplyLeave"; 
import LeaveStatus from "./components/EmployeeComponents/LeaveComponents/LeaveStatus"; 
import LeaveBalance from "./components/EmployeeComponents/LeaveComponents/LeaveBalance"; 
import ShiftDashboard from "./components/EmployeeComponents/ShiftComponents/ShiftDashboard"; 
import ShiftRequest from "./components/EmployeeComponents/ShiftComponents/ShiftRequest"; 
import ShiftStatus from "./components/EmployeeComponents/ShiftComponents/ShiftStatus"; 
import ManagerDashboard from "./components/ManagerComponents/ManagerDashboard";
import EmployeeDetails from "./components/ManagerComponents/DetailsComponent/EmpDetails";
import LeaveRequests from "./components/ManagerComponents/LeaveComponents/LeaveRequests";
import ApproveRejectLeave from "./components/ManagerComponents/LeaveComponents/ApproveorRejectLeave";
import EmployeeLeaveBalance from "./components/ManagerComponents/LeaveComponents/EmployeeLeaveBalance";
import EmployeeShiftRequests from "./components/ManagerComponents/ShiftComponents/EmployeeShiftRequests";
import ApproveorRejectShift from "./components/ManagerComponents/ShiftComponents/ApproveorRejectShift";
import AllAssignments from "./components/ManagerComponents/ShiftComponents/AllAssignments";
import AssignShifts from "./components/ManagerComponents/ShiftComponents/AssignShifts";
import Reports from "./components/ManagerComponents/ReportComponents/Reports";
import EmployeeAttendanceReports from "./components/ManagerComponents/ReportComponents/EmployeeAttendanceReports";
import EmployeeLeaveReports from "./components/ManagerComponents/ReportComponents/EmployeeLeaveReports";
import EmployeeShiftReports from "./components/ManagerComponents/ReportComponents/EmployeeShiftReports";
import IndividualReports from "./components/EmployeeComponents/ReportComponents/IndividualReports";
import AttendanceReports from "./components/EmployeeComponents/ReportComponents/AttendanceReports";
import LeaveReports from "./components/EmployeeComponents/ReportComponents/LeaveRepots";
import ShiftReports from "./components/EmployeeComponents/ReportComponents/ShiftReports";
import Profile from "./components/EmployeeComponents/ProfileComponents/Profile";
import ManagerProfile from "./components/ManagerComponents/ProfileComponents/ManagerProfile";
import AssignedShifts from "./components/EmployeeComponents/ShiftComponents/AssignedShifts";

function App() {
const [isLoggedIn, setIsLoggedIn] = useState(false); // Track login status
const [userRole, setUserRole] = useState(""); // Track user role (EMPLOYEE/MANAGER)
const [userloggedIn, setUserloggedIn] = useState({});
const [sessionExpired, setSessionExpired] = useState(false);
const [loginDetails, setLoginDetails] = useState(null); // Track user role (EMPLOYEE/MANAGER)
const isLoggedInCheck=()=>{
  if(localStorage.getItem("userLoggedIn")){
    return true
  }else{
    return false
  }
}
const checkTokenExpiration = () => {
  const token = localStorage.getItem('jwtToken');
  if (token) {
    const decodedToken = jwtDecode(token);
    const currentTime = Date.now();
    const expirationTime = decodedToken.exp * 1000;
    if (currentTime >= expirationTime) {
      setSessionExpired(true);
      setIsLoggedIn(false);
      localStorage.clear();
    }
  }
};
useEffect(() => {
  const userdata = localStorage.getItem('userLoggedIn');
  if (userdata) {
    setLoginDetails(JSON.parse(userdata));
    console.log(userdata);
    setIsLoggedIn(true);
    const token = localStorage.getItem('jwtToken');
    if (token) {
      const decodedToken = jwtDecode(token);
      const currentTime = Date.now();
      const expirationTime = decodedToken.exp * 1000;
      const timeUntilExpiration = expirationTime - currentTime;

      if (timeUntilExpiration > 0) {
        setTimeout(() => {
          setSessionExpired(true);
          setIsLoggedIn(false);
          localStorage.clear();
        }, timeUntilExpiration);
      } else {
        setSessionExpired(true);
        setIsLoggedIn(false);
        localStorage.clear();
      }
    }
    const intervalId = setInterval(checkTokenExpiration, 60000);
    return () => clearInterval(intervalId);
  }
}, []);
useEffect(()=>{
 var obj=JSON.parse(localStorage.getItem("userLoggedIn")) //{"sub":"akhila@gmail.com","role":"EMPLOYEE","employeeId":"2389413","emailId":"akhila@gmail.com","iat":1744871816,"exp":1744875416}
  console.log('isLoggedInCheck:',isLoggedInCheck())
  setIsLoggedIn(isLoggedInCheck());
  if(isLoggedInCheck()){
    console.log(localStorage.getItem("userLoggedIn"))
    setUserloggedIn(obj)
    console.log(obj.role)
    setUserRole(obj.role)
  }
},[])
  const handleLogout = () => {
    localStorage.clear(); // Clears all stored user data
    setIsLoggedIn(false); // Update state to reflect logout
    setUserRole(""); // Reset the user role state
    window.location.refresh = "/"; // Redirect to login page
  };

  return (
    <Router>
      <div className="dashboard-container">
        {/* Navigation Menu */}
        {sessionExpired && (
        <div className='modal-overlay'>
          <div className='modal-container'>
            <p className='session-expired'>Session expired. Please log in again.</p>
            <button onClick={() => { setSessionExpired(false); }}>OK</button>
          </div>
        </div>
      )}
        <nav className="dashboard-nav">
          <ul>
            {/* {!isLoggedIn && <li><Link to="/">Login</Link></li>} */}
            {isLoggedIn && userRole === "EMPLOYEE" &&(
              <>
                <li><Link to="/employee-dashboard ">Attendance</Link></li>
                <li><Link to="/leaves">Leaves</Link></li>
                <li><Link to="/shifts">Shifts</Link></li>
                <li><Link to="/reports">Reports</Link></li>
                <li><Link to="/profile">Profile</Link></li>
                <li><button onClick={handleLogout}>Logout</button></li> {/* Updated Logout Button */}
              </> 
            )}
            {isLoggedIn && userRole === "MANAGER" && (
              <>
                <li><Link to="/manager-dashboard">Attendance</Link></li>
                <li><Link to="/employee-details">Employee Details</Link></li>
                <li><Link to="/leave-requests">Leave Requests</Link></li>
                <li><Link to="/shift-requests">Shift Requests</Link></li>
                <li><Link to="/reports">Reports</Link></li>
                <li><Link to="/profile">Profile</Link></li>
                <li><button onClick={handleLogout} style={{ backgroundColor: "#2f3e9e", color: "white", padding: "10px 20px", border: "none", borderRadius: "5px", cursor: "pointer", fontStyle: "bold" }}>Logout</button></li>
              </>
            )}
          </ul>
        </nav>
        {/*Routing Logic*/}
        <Routes>
        {/* Redirecting based on login and role */}
        <Route path="/" element={
          isLoggedIn ? (userRole === "EMPLOYEE" 
            ? <Navigate to="/employee-dashboard" /> : <Navigate to="/manager-dashboard" />) 
          : (<LoginPage setLoggedIn={setIsLoggedIn} setUserRole={setUserRole} />)
        } />

        {/* Employee-specific Routes (Accessible only to employees) */}
        {isLoggedIn && userRole === "EMPLOYEE" && (
          <>
            <Route path="/employee-dashboard" element={<EmployeeDashboard value={userloggedIn} setLoggedIn={setIsLoggedIn}/>}/>
            <Route path="/leaves" element={<LeaveDashboard />} />
            <Route path="/leaves/apply" element={<ApplyLeave />} />
            <Route path="/leaves/status" element={<LeaveStatus />} />
            <Route path="/leaves/balance" element={<LeaveBalance />} />
            <Route path="/shifts" element={<ShiftDashboard />} />
            <Route path="/shifts/request" element={<ShiftRequest />} />
            <Route path="/shifts/status" element={<ShiftStatus />} />
            <Route path="/shifts/assigned" element={<AssignedShifts />} />
            <Route path="/reports" element={<IndividualReports />} />
            <Route path="/reports/attendance" element={<AttendanceReports />} />
            <Route path="/reports/leaves" element={<LeaveReports />} />
            <Route path="/reports/shifts" element={<ShiftReports />} />
            <Route path="/profile" element={<Profile />} />
          </>
        )}

        {/* ✅ Manager-specific Routes (Accessible only to managers) */}
        {isLoggedIn && userRole === "MANAGER" && (
          <>
            <Route path="/manager-dashboard" element={<ManagerDashboard value={userloggedIn.employeeId}/>}/>
            <Route path="/employee-details" element={<EmployeeDetails />} />
            <Route path="/leave-requests" element={<LeaveRequests />} />
            <Route path="/approve-reject-leave" element={<ApproveRejectLeave />} />
            <Route path="/leave-balance" element={<EmployeeLeaveBalance />} />
            <Route path="/shift-requests" element={<EmployeeShiftRequests />} />
            <Route path="/approve-reject-requests" element={<ApproveorRejectShift />} />
            <Route path="/show-assignments" element={<AllAssignments />} />
            <Route path="/create-shifts" element={<AssignShifts />} />
            <Route path="/reports" element={<Reports />} />
            <Route path="/attendance-reports" element={<EmployeeAttendanceReports />} />
            <Route path="/leave-reports" element={<EmployeeLeaveReports />} />
            <Route path="/shift-reports" element={<EmployeeShiftReports />} />
            <Route path="/profile" element={<ManagerProfile />} />
          </>
        )}

        {/* Redirect all unknown routes to home */}
        <Route path="*" element={<Navigate to="/" />} />
      </Routes>
      </div>
    </Router>
  );
}

export default App;