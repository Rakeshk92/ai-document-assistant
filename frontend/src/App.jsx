import { useEffect, useMemo, useState } from "react";
import {
  fetchDocuments,
  uploadDocument,
  searchDocuments,
  askQuestion,
  deleteDocument,
} from "./api/documentApi";

function App() {
  const [selectedFile, setSelectedFile] = useState(null);
  const [documents, setDocuments] = useState([]);
  const [message, setMessage] = useState("");
  const [searchKeyword, setSearchKeyword] = useState("");
  const [question, setQuestion] = useState("");
  const [chatMessages, setChatMessages] = useState([]);
  const [loading, setLoading] = useState(false);
  const [theme, setTheme] = useState(localStorage.getItem("theme") || "system");

  const systemTheme = window.matchMedia("(prefers-color-scheme: dark)").matches
    ? "dark"
    : "light";
  const activeTheme = theme === "system" ? systemTheme : theme;
  const isDark = activeTheme === "dark";

  useEffect(() => {
    localStorage.setItem("theme", theme);
  }, [theme]);

  const colors = useMemo(
    () => ({
      page: isDark ? "#0f172a" : "#f8fafc",
      sidebar: isDark ? "#111827" : "#ffffff",
      panel: isDark ? "#1e293b" : "#ffffff",
      border: isDark ? "#334155" : "#e5e7eb",
      text: isDark ? "#f8fafc" : "#111827",
      subtext: isDark ? "#cbd5e1" : "#6b7280",
      input: isDark ? "#0b1220" : "#ffffff",
      userBubble: "#2563eb",
      aiBubble: isDark ? "#1f2937" : "#f3f4f6",
      primary: "#2563eb",
      danger: "#dc2626",
      modeBg: isDark ? "#0f172a" : "#eef2ff",
      modeText: isDark ? "#93c5fd" : "#1d4ed8",
    }),
    [isDark]
  );

  const currentMode =
    chatMessages.length > 0 &&
    chatMessages[chatMessages.length - 1].role === "assistant"
      ? chatMessages[chatMessages.length - 1].mode || "Fallback Retrieval"
      : "Fallback Retrieval";

  const loadDocuments = async () => {
    try {
      const data = await fetchDocuments();
      setDocuments(data);
    } catch (error) {
      console.error(error);
      setMessage("Failed to load documents.");
    }
  };

  useEffect(() => {
    loadDocuments();
  }, []);

  const handleUpload = async () => {
    if (!selectedFile) {
      setMessage("Select a file first.");
      return;
    }

    try {
      setLoading(true);
      const uploaded = await uploadDocument(selectedFile);
      setMessage(`Uploaded: ${uploaded.fileName}`);
      setSelectedFile(null);
      await loadDocuments();
    } catch (error) {
      console.error(error);
      setMessage("Upload failed.");
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async () => {
    if (!searchKeyword.trim()) {
      await loadDocuments();
      return;
    }

    try {
      setLoading(true);
      const results = await searchDocuments(searchKeyword);
      setDocuments(results);
      setMessage("");
    } catch (error) {
      console.error(error);
      setMessage("Search failed.");
    } finally {
      setLoading(false);
    }
  };

  const handleAsk = async () => {
    if (!question.trim()) {
      return;
    }

    const userMessage = {
      role: "user",
      content: question,
    };

    setChatMessages((prev) => [...prev, userMessage]);
    const currentQuestion = question;
    setQuestion("");

    try {
      setLoading(true);
      const result = await askQuestion(currentQuestion);

      const aiMessage = {
        role: "assistant",
        content: result.answer,
        sourceDocument: result.sourceDocument,
        chunkIndex: result.chunkIndex,
        mode: result.mode,
      };

      setChatMessages((prev) => [...prev, aiMessage]);
      setMessage("");
    } catch (error) {
      console.error(error);
      setChatMessages((prev) => [
        ...prev,
        {
          role: "assistant",
          content: "Sorry, I could not answer that question.",
          mode: "Fallback Retrieval",
        },
      ]);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id) => {
    try {
      setLoading(true);
      await deleteDocument(id);
      await loadDocuments();
      setMessage("Document deleted successfully.");
    } catch (error) {
      console.error(error);
      setMessage("Delete failed.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      style={{
        minHeight: "100vh",
        display: "grid",
        gridTemplateColumns: "320px 1fr",
        backgroundColor: colors.page,
        color: colors.text,
        fontFamily: "Inter, Arial, sans-serif",
      }}
    >
      <aside
        style={{
          borderRight: `1px solid ${colors.border}`,
          backgroundColor: colors.sidebar,
          padding: "1rem",
          display: "flex",
          flexDirection: "column",
          gap: "1rem",
        }}
      >
        <div>
          <h2 style={{ margin: 0 }}>AI Docs</h2>
          <p style={{ color: colors.subtext, marginTop: "0.4rem" }}>
            ChatGPT-style document assistant
          </p>
        </div>

        <select
          value={theme}
          onChange={(e) => setTheme(e.target.value)}
          style={{
            padding: "0.7rem",
            borderRadius: "10px",
            border: `1px solid ${colors.border}`,
            backgroundColor: colors.input,
            color: colors.text,
          }}
        >
          <option value="light">Light</option>
          <option value="dark">Dark</option>
          <option value="system">System</option>
        </select>

        <div
          style={{
            backgroundColor: colors.panel,
            border: `1px solid ${colors.border}`,
            borderRadius: "14px",
            padding: "1rem",
          }}
        >
          <h3 style={{ marginTop: 0 }}>Upload</h3>
          <input
            type="file"
            accept=".txt,.pdf"
            onChange={(e) => setSelectedFile(e.target.files[0])}
            style={{ marginBottom: "0.8rem", width: "100%" }}
          />
          <button
            onClick={handleUpload}
            style={{
              width: "100%",
              padding: "0.75rem",
              border: "none",
              borderRadius: "10px",
              backgroundColor: colors.primary,
              color: "white",
              cursor: "pointer",
              fontWeight: 600,
            }}
          >
            Upload document
          </button>
        </div>

        <div
          style={{
            backgroundColor: colors.panel,
            border: `1px solid ${colors.border}`,
            borderRadius: "14px",
            padding: "1rem",
          }}
        >
          <h3 style={{ marginTop: 0 }}>Search documents</h3>
          <input
            value={searchKeyword}
            onChange={(e) => setSearchKeyword(e.target.value)}
            placeholder="Keyword..."
            style={{
              width: "100%",
              padding: "0.75rem",
              borderRadius: "10px",
              border: `1px solid ${colors.border}`,
              backgroundColor: colors.input,
              color: colors.text,
              marginBottom: "0.75rem",
              boxSizing: "border-box",
            }}
          />
          <button
            onClick={handleSearch}
            style={{
              width: "100%",
              padding: "0.75rem",
              border: "none",
              borderRadius: "10px",
              backgroundColor: colors.primary,
              color: "white",
              cursor: "pointer",
              fontWeight: 600,
            }}
          >
            Search
          </button>
        </div>

        <div
          style={{
            backgroundColor: colors.panel,
            border: `1px solid ${colors.border}`,
            borderRadius: "14px",
            padding: "1rem",
            overflowY: "auto",
            flex: 1,
          }}
        >
          <h3 style={{ marginTop: 0 }}>Documents</h3>

          {documents.length === 0 ? (
            <p style={{ color: colors.subtext }}>No documents found.</p>
          ) : (
            documents.map((doc) => (
              <div
                key={doc.id}
                style={{
                  border: `1px solid ${colors.border}`,
                  borderRadius: "10px",
                  padding: "0.8rem",
                  marginBottom: "0.8rem",
                }}
              >
                <div style={{ fontWeight: 600, marginBottom: "0.25rem" }}>
                  {doc.fileName}
                </div>
                <div
                  style={{
                    color: colors.subtext,
                    fontSize: "0.85rem",
                    marginBottom: "0.5rem",
                  }}
                >
                  {doc.fileType}
                </div>
                <button
                  onClick={() => handleDelete(doc.id)}
                  style={{
                    border: "none",
                    borderRadius: "8px",
                    padding: "0.5rem 0.8rem",
                    backgroundColor: colors.danger,
                    color: "white",
                    cursor: "pointer",
                  }}
                >
                  Delete
                </button>
              </div>
            ))
          )}
        </div>
      </aside>

      <main
        style={{
          display: "flex",
          flexDirection: "column",
          height: "100vh",
        }}
      >
        <div
          style={{
            padding: "1rem 1.5rem",
            borderBottom: `1px solid ${colors.border}`,
            backgroundColor: colors.panel,
          }}
        >
          <div style={{ fontSize: "1.15rem", fontWeight: 700 }}>
            Document Chat
          </div>
          <div style={{ color: colors.subtext, marginTop: "0.2rem" }}>
            Ask questions grounded in your uploaded files
          </div>

          <div
            style={{
              marginTop: "0.6rem",
              display: "inline-block",
              padding: "0.35rem 0.7rem",
              borderRadius: "999px",
              fontSize: "0.8rem",
              fontWeight: 600,
              backgroundColor: colors.modeBg,
              color: colors.modeText,
              border: `1px solid ${colors.border}`,
            }}
          >
            Mode: {currentMode}
          </div>
        </div>

        <div
          style={{
            flex: 1,
            overflowY: "auto",
            padding: "1.5rem",
            display: "flex",
            flexDirection: "column",
            gap: "1rem",
          }}
        >
          {chatMessages.length === 0 ? (
            <div
              style={{
                maxWidth: "760px",
                margin: "4rem auto 0",
                textAlign: "center",
                color: colors.subtext,
              }}
            >
              Upload a document and start asking questions.
            </div>
          ) : (
            chatMessages.map((msg, index) => (
              <div
                key={index}
                style={{
                  alignSelf: msg.role === "user" ? "flex-end" : "flex-start",
                  maxWidth: "75%",
                  backgroundColor:
                    msg.role === "user" ? colors.userBubble : colors.aiBubble,
                  color: msg.role === "user" ? "white" : colors.text,
                  padding: "1rem",
                  borderRadius: "16px",
                  lineHeight: 1.5,
                  boxShadow: "0 2px 10px rgba(0,0,0,0.08)",
                }}
              >
                <div>{msg.content}</div>

                {msg.role === "assistant" && msg.sourceDocument && (
                  <div
                    style={{
                      marginTop: "0.75rem",
                      fontSize: "0.82rem",
                      color: colors.subtext,
                    }}
                  >
                    Source: {msg.sourceDocument} • Chunk {msg.chunkIndex ?? "N/A"}
                  </div>
                )}
              </div>
            ))
          )}

          {loading && (
            <div
              style={{
                alignSelf: "flex-start",
                backgroundColor: colors.aiBubble,
                color: colors.text,
                padding: "1rem",
                borderRadius: "16px",
              }}
            >
              Thinking...
            </div>
          )}
        </div>

        <div
          style={{
            padding: "1rem 1.5rem",
            borderTop: `1px solid ${colors.border}`,
            backgroundColor: colors.panel,
          }}
        >
          {message && (
            <div
              style={{
                marginBottom: "0.75rem",
                color: colors.subtext,
                fontSize: "0.9rem",
              }}
            >
              {message}
            </div>
          )}

          <div
            style={{
              display: "grid",
              gridTemplateColumns: "1fr auto",
              gap: "0.75rem",
              alignItems: "center",
            }}
          >
            <input
              value={question}
              onChange={(e) => setQuestion(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === "Enter" && !loading) {
                  handleAsk();
                }
              }}
              placeholder="Message your documents..."
              style={{
                width: "100%",
                padding: "1rem",
                borderRadius: "14px",
                border: `1px solid ${colors.border}`,
                backgroundColor: colors.input,
                color: colors.text,
                boxSizing: "border-box",
                fontSize: "1rem",
              }}
            />

            <button
              onClick={handleAsk}
              disabled={loading}
              style={{
                padding: "1rem 1.2rem",
                border: "none",
                borderRadius: "14px",
                backgroundColor: colors.primary,
                color: "white",
                cursor: "pointer",
                fontWeight: 700,
                opacity: loading ? 0.7 : 1,
              }}
            >
              Send
            </button>
          </div>
        </div>
      </main>
    </div>
  );
}

export default App;