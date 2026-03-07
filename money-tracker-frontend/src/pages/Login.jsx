import {useContext, useState} from "react";
import {Link, useNavigate} from "react-router-dom";
import {assets} from "../assets/assets.js";
import Input from "../components/input.jsx";
import {validateEmail} from "../util/validation.js";
import axiosConfig from "../util/axiosConfig.jsx";
import {API_ENDPOINTS} from "../util/apiEndpoints.js";
import {AppContext} from "../context/AppContext.jsx";
import {LoaderCircle} from "lucide-react";

const Login = () => {

    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState(null);
    const navigate = useNavigate();
    const [isLoading, setIsLoading] = useState(false);
    const { setUser } = useContext(AppContext);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setIsLoading(true);
        setError(null);

        if (!password.trim()) {
            setError("Password cannot be empty.");
            setIsLoading(false);
            return;
        }
        if (!validateEmail(email)) {
            setError("Please enter valid email address");
            setIsLoading(false);
            return;
        }
        try{
            const response = await axiosConfig.post(API_ENDPOINTS.LOGIN, {
                email,
                password
            })
            const {token, user} = response.data;
            if (token) {
                localStorage.setItem("token", token);
                setUser(user);
                navigate("/dashboard");
            }
        } catch (error) {
            if (error.response && error.response.data.message) {
                setError(error.response.data.message);
            } else {
                setError(error.message || "Login failed. Please try again.");
            }
        } finally {
            setIsLoading(false);
        }

    }

    return (
        <div className="h-screen w-full relative flex items-center justify-center overflow-hidden">
            <img src={assets.login_bg} alt="background"
                 className="absolute inset-0 w-full h-full object-cover filter blur-sm pointer-events-none"/>
            <div className="relative z-10 w-full max-w-lg  px-6">
                <div
                    className="bg-white bg-opacity-95 backdrop-blur-sm rounded-lg shadow-2xl p-8 max-h-[90vh] overflow-y-auto">
                    <h3 className="text-2xl font-semibold text-black text-center mb-2">
                        Welcome Back!</h3>
                    <p className="text-sm text-slate-700 text-center mb-8">
                        Please enter your details to login
                    </p>
                    <form onSubmit={handleSubmit} className="space-y-4">
                        <Input
                            value={email}
                            onchange={(e) => setEmail(e.target.value)}
                            label="Email Address"
                            placeholder="example@gmail.com"
                            type="text"
                        />
                        <Input
                            value={password}
                            onchange={(e) => setPassword(e.target.value)}
                            label="Password"
                            placeholder="********"
                            type="password"
                        />
                        {error && (
                            <p className="text-red-500 text-sm text-center bg-red-50 p-2 rounded">{error}</p>
                        )}
                        <button
                            disabled={isLoading}
                            className={`btn-primary w-full py-3 text-lg font-medium flex items-center justify-center gap-2 ${isLoading? 'opacity-60 cursor-not-allowed' : ''}`}
                            type="submit">
                            {isLoading ? (
                                <>
                                    <LoaderCircle className="animate-spin w-5 h-5"/>
                                    Logging in...
                                </>
                            ) : "Login"}
                        </button>
                        <p className="text-sm text-slate-800 text-center mt-6">
                            Don't have an account?
                            <Link to="/signup"
                                  className="font-medium text-blue-600 underline hover:text-blue-700 transition-colors">Signup</Link>
                        </p>
                    </form>
                </div>
            </div>

        </div>
    )
}

export default Login;
