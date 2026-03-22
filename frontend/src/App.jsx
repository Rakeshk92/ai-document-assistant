import { useEffect, useState } from "react";
import axios from "axios";

function App() {
  const [message, setMessage] = useState("Loading backend message...");

  useEffect(() => {
    axios
      .get("http://localhost:8080/api/health")
      .then((response) => {
        setMessage(response.data);
      })
      .catch((error) => {
        console.error("Error calling backend:", error);
        setMessage("Failed to connect to backend.");
      });
  }, []);

  return (
    <div style={{ padding: "2rem", fontFamily: "Arial" }}>
      <h1>AI Document Assistant</h1>
      <p>{message}</p>
    </div>
  );
}

export default App;