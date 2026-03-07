export const BASE_URL = "https://money-tracker-3nbw.onrender.com/api/v1";
const CLOUDINARY_CLOUD_NAME = "dj1tayyvn";

export const API_ENDPOINTS = {
    LOGIN: "/login",
    REGISTER: "/register",
    UPLOAD_IMAGE: `https://api.cloudinary.com/v1_1/${CLOUDINARY_CLOUD_NAME}/image/upload`,
    GET_USER_INFO: "/profile",
}
