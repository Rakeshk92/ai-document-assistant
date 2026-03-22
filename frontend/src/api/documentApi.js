import axios from "axios";

const BASE_URL = "http://localhost:8080/api/documents";

export const uploadDocument = async (file) => {
  const formData = new FormData();
  formData.append("file", file);

  const response = await axios.post(`${BASE_URL}/upload`, formData, {
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });

  return response.data;
};

export const fetchDocuments = async () => {
  const response = await axios.get(BASE_URL);
  return response.data;
};