import { useEffect, useState } from "react";
import { fetchDocuments, uploadDocument } from "./api/documentApi";

function App() {
  const [selectedFile, setSelectedFile] = useState(null);
  const [documents, setDocuments] = useState([]);
  const [message, setMessage] = useState("");

  const loadDocuments = async () => {
    try {
      const data = await fetchDocuments();
      setDocuments(data);
    } catch (error) {
      console.error("Error fetching documents:", error);
      setMessage("Failed to load documents.");
    }
  };

  useEffect(() => {
    loadDocuments();
  }, []);

  const handleFileChange = (event) => {
    setSelectedFile(event.target.files[0]);
  };

  const handleUpload = async () => {
    if (!selectedFile) {
      setMessage("Please select a file first.");
      return;
    }

    try {
      const uploadedDoc = await uploadDocument(selectedFile);
      setMessage(`Uploaded successfully: ${uploadedDoc.fileName}`);
      setSelectedFile(null);
      loadDocuments();
    } catch (error) {
      console.error("Upload error:", error);
      setMessage(
        error?.response?.data?.message || "File upload failed. Only .txt files are supported right now."
      );
    }
  };

  return (
    <div style={{ padding: "2rem", fontFamily: "Arial", maxWidth: "900px", margin: "0 auto" }}>
      <h1>AI Document Assistant</h1>
      <p>Upload a text file and store it in the database.</p>

      <div style={{ marginBottom: "1.5rem" }}>
        <input type="file" accept=".txt" onChange={handleFileChange} />
        <button
          onClick={handleUpload}
          style={{
            marginLeft: "1rem",
            padding: "0.5rem 1rem",
            cursor: "pointer"
          }}
        >
          Upload
        </button>
      </div>

      {message && <p><strong>{message}</strong></p>}

      <h2>Uploaded Documents</h2>

      {documents.length === 0 ? (
        <p>No documents uploaded yet.</p>
      ) : (
        <div>
          {documents.map((doc) => (
            <div
              key={doc.id}
              style={{
                border: "1px solid #ccc",
                borderRadius: "8px",
                padding: "1rem",
                marginBottom: "1rem"
              }}
            >
              <h3>{doc.fileName}</h3>
              <p><strong>Type:</strong> {doc.fileType}</p>
              <p><strong>Uploaded At:</strong> {doc.uploadedAt}</p>
              <p>
                <strong>Content Preview:</strong>{" "}
                {doc.content.length > 200 ? doc.content.substring(0, 200) + "..." : doc.content}
              </p>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default App;