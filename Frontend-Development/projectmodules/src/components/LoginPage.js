import React, { useState } from "react";
import { jwtDecode } from "jwt-decode"; 
import { useNavigate } from "react-router-dom";
import "bootstrap/dist/css/bootstrap.min.css"; // Bootstrap styles

const LoginPage = ({ setLoggedIn, setUserRole }) => {
  const [identifier, setIdentifier] = useState("");
  const [password, setPassword] = useState("");
  const [errors, setErrors] = useState("");
  const [isLoading, setIsLoading] = useState(false); 
  const navigate = useNavigate(); 

  const HandleLogin = async (event) => {
    event.preventDefault();
    if (!identifier || !password) {
      setErrors("Both Employee ID/Email and Password are required.");
      return;
    }
    setIsLoading(true); 
    try {
      const loginData = {
        email: identifier.trim(),
        password: password.trim(),
      };
      const response = await fetch("http://localhost:8091/employee/login", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(loginData),
      });

      if (response.ok) {
        const token = await response.text();
        try {
          const decodedToken = jwtDecode(token);
          localStorage.setItem("jwtToken", token);
         const tokenDetails= {"sub":decodedToken.sub,"role":decodedToken.role,"employeeId":decodedToken.employeeId,"emailId":decodedToken.emailId,"iat":decodedToken.iat,"exp":decodedToken.exp}
          localStorage.setItem("userLoggedIn", JSON.stringify(tokenDetails));
          const role = decodedToken.role;
          setLoggedIn(true);
          setUserRole(role);
          role === "EMPLOYEE" ? navigate("/employee-dashboard") : navigate("/manager-dashboard");
        } catch {
          setErrors("Invalid token received. Please try again.");
        }
      } else {
        const errorData = await response.json();
        setErrors(errorData.message || "Login failed. Please check your credentials.");
      }
    } catch {
      setErrors("Login failed. Please check your credentials.");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div
      className="d-flex align-items-center justify-content-center min-vh-100"
      style={{
        backgroundImage: "url('https://t4.ftcdn.net/jpg/06/57/87/47/360_F_657874794_myYcKACL3ipw93UHYnsBlWgwSudZdjrH.jpg')",
        backgroundSize: "cover",
        backgroundPosition: "center",
        backgroundRepeat: "no-repeat",
      }}
    >
      <form
        className="p-4 rounded shadow-lg"
        style={{
          maxWidth: "400px",
          width: "100%",
          backgroundColor: "rgba(255, 255, 255, 0.85)", // Slight transparency
        }}
        onSubmit={HandleLogin}
      >
        <div className="mb-3">
        <h3 className="text-center mb-3">Login</h3>
          <label htmlFor="identifier" className="form-label">
            Email:
          </label>
          <input
            type="text"
            className="form-control bg-light border-0"
            id="identifier"
            value={identifier}
            onChange={(e) => setIdentifier(e.target.value)}
            placeholder="Enter Employee ID or Email"
            required
          />
        </div>

        <div className="mb-3">
          <label htmlFor="password" className="form-label">
            Password:
          </label>
          <input
            type="password"
            className="form-control bg-light border-0"
            id="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="Enter your Password"
            required
          />
        </div>

        <button type="submit" className="btn btn-warning w-100" disabled={isLoading}>
          {isLoading ? "Logging in..." : "Login"}
        </button>

        {errors && <div className="text-danger text-center mt-3">{errors}</div>}
      </form>
    </div>
  );
};

export default LoginPage;
