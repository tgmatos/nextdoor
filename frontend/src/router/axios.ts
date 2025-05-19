import axios from "axios";

const apiClient = axios.create({
    baseURL: "http://localhost:4000",
    timeout: 5000,
    headers: {
      "Content-Type": "application/json",
      "Accept": "application/json"
    }
});

export default apiClient;

