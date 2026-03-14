export const BASE_URL = "https://money-tracker-3nbw.onrender.com/api/v1";
const CLOUDINARY_CLOUD_NAME = "dj1tayyvn";

export const API_ENDPOINTS = {
    LOGIN: "/login",
    REGISTER: "/register",
    UPLOAD_IMAGE: `https://api.cloudinary.com/v1_1/${CLOUDINARY_CLOUD_NAME}/image/upload`,
    GET_USER_INFO: "/profile",
    GET_ALL_CATEGORIES: "/categories",
    ADD_CATEGORY: "/categories",
    UPDATE_CATEGORY: (categoryId) => `/categories/${categoryId}`,
    GET_ALL_INCOME: "/incomes",
    GET_ALL_EXPENSE: "/expenses",
    CATEGORY_BY_TYPE: (type) => `/categories/${type}`,
    ADD_INCOME: "/incomes",
    ADD_EXPENSE: "/expenses",
    DELETE_INCOME: (id) => `/incomes/${id}`,
    DELETE_EXPENSE: (id) => `/expenses/${id}`,
    INCOME_EXCEL_DOWNLOAD: "/incomes/download/excel",
    EXPENSE_EXCEL_DOWNLOAD: "/expenses/download/excel",
    EMAIL_INCOME: "/incomes/email",
    EMAIL_EXPENSE: "/expenses/email",
    APPLY_FILTERS: "/filter",
    DASHBOARD_DATA: "/dashboard"
}
