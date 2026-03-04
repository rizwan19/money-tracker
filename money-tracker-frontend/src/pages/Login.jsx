import {useState} from "react";
import {Link, useNavigate} from "react-router-dom";
import {assets} from "../assets/assets.js";
import Input from "../components/input.jsx";

const Login = () => {

    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState(null);
    const navigate = useNavigate();

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
                    <form className="space-y-4">
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
                            className="w-full py-3 text-lg font-medium rounded-md bg-blue-600 text-white hover:bg-blue-700 active:bg-blue-800 cursor-pointer transition-colors"
                            type="submit">
                            Login
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