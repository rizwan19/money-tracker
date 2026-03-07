import {useContext, useEffect} from "react";
import {AppContext} from "../context/AppContext.jsx";
import {useNavigate} from "react-router-dom";
import axiosConfig from "../util/axiosConfig.jsx";
import {API_ENDPOINTS} from "../util/apiEndpoints.js";

export const useUser = () => {
    const {user, setUser, clearUser} = useContext(AppContext);
    const navigate = useNavigate();

    useEffect(() => {
        if (user) {
            return;
        }
        let isMounted = true;
        const fetchUser = async () => {
            try{
                const response = await axiosConfig.get(API_ENDPOINTS.GET_USER_INFO);

                if (isMounted && response.data) {
                    setUser(response.data);
                }
            } catch (error) {
                console.error(error);
                if (isMounted) {
                    clearUser();
                    navigate("/login");
                }
            }
        }
        fetchUser();

        return () => {
            isMounted = false;
        }
    }, [setUser, clearUser, navigate]);
}