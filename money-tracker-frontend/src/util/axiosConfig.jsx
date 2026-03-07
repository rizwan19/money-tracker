import axios from "axios";

const axiosConfig = axios.create({
    // baseURL: "https://money-tracker-3nbw.onrender.com/api/v1",
    baseURL: "http://localhost:8080/api/v1.0",
    headers: {
        "Content-Type": "application/json",
        Accept: "application/json"
    }
});

const excludeEndpoints = ["/login", "/register", "/activate", "/health", "/status"];

axiosConfig.interceptors.request.use(config => {
    const shouldSkipToken = excludeEndpoints.some((endpoint) => {
        return config.url?.includes(endpoint);
    })
    if(!shouldSkipToken) {
        const token = localStorage.getItem("token");
        if (token) {
            config.headers["Authorization"] = `Bearer ${token}`;
        }
    }
    return config;
}, (error) => {
    return Promise.reject(error);
})

axiosConfig.interceptors.response.use(response => {
    return response;
}, (error) => {
    if (error.response && error.response.status === 401) {
        window.location.href = "/login";
    }
    else if (error.response && error.response.status === 500) {
        console.log("Server error:", error.response.data);
    }
    return Promise.reject(error);
})

export default axiosConfig;
