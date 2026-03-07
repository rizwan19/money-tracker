import {assets} from "../assets/assets.js";
import {useState} from "react";
import {Link, useNavigate} from "react-router-dom";
import Input from "../components/input.jsx";
import {validateEmail} from "../util/validation.js";
import axiosConfig from "../util/axiosConfig.jsx";
import {API_ENDPOINTS} from "../util/apiEndpoints.js";
import toast from "react-hot-toast";
import {LoaderCircle} from "lucide-react";
import ProfilePhotoSelector from "../components/ProfilePhotoSelector.jsx";
import uploadProfileImage from "../util/uploadProfileImage.js";

const Signup = () => {
    const [fullName, setFullName] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState(null);
    const navigate = useNavigate();
    const [isLoading, setIsLoading] = useState(false);
    const [profileImage, setProfileImage] = useState(null);

    const handleSignup = async (e) => {
        e.preventDefault();
        setIsLoading(true);
        setError(null);

        if (!fullName.trim()) {
            setError("Full name cannot be empty.");
            setIsLoading(false);
            return;
        }
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
        setEmail("");

        try{
            let profileImageUrl = "";
            if (profileImage) {
                const imageUrl = await uploadProfileImage(profileImage);
                profileImageUrl = imageUrl || "";
            }
            const response = await axiosConfig.post(API_ENDPOINTS.REGISTER, {
                fullName,
                email,
                password,
                profileImageUrl
            })
            if (response.status === 201) {
                toast.success("Account created successfully! Please login.");
                navigate("/login");
            }
        } catch(err) {
            console.error("Something went wrong: " + err);
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="h-screen w-full relative flex items-center justify-center overflow-hidden">
            <img src={assets.login_bg} alt="background" className="absolute inset-0 w-full h-full object-cover filter blur-sm pointer-events-none"/>
            <div className="relative z-10 w-full max-w-lg  px-6">
                <div className="bg-white bg-opacity-95 backdrop-blur-sm rounded-lg shadow-2xl p-8 max-h-[90vh] overflow-y-auto">
                    <h3 className="text-2xl font-semibold text-black text-center mb-2">
                        Create an Account</h3>
                    <p className="text-sm text-slate-700 text-center mb-8">
                        Start tracking your expense by joining with us.
                    </p>
                    <form className="space-y-4" onSubmit={handleSignup}>
                        <div className="flex justify-center mb-6">
                            <ProfilePhotoSelector image={profileImage} setImage={setProfileImage}/>
                        </div>
                        <div className="grid grid-cols-2 md:grid-cols-2 gap-4">
                            <Input
                                value={fullName}
                                onchange={(e) => setFullName(e.target.value)}
                                label="Full Name"
                                placeholder="Enter your full name"
                                type="text"
                            />
                            <Input
                                value={email}
                                onchange={(e) => setEmail(e.target.value)}
                                label="Email Address"
                                placeholder="example@gmail.com"
                                type="text"
                            />
                            <div className="col-span-2">
                                <Input
                                    value={password}
                                    onchange={(e) => setPassword(e.target.value)}
                                    label="Password"
                                    placeholder="********"
                                    type="password"
                                />
                            </div>
                        </div>
                        {error && (
                            <p className="text-red-500 text-sm text-center bg-red-50 p-2 rounded">{error}</p>
                        )}
                        <button
                            disabled={isLoading}
                            className={`btn-primary w-full py-3 text-lg font-medium flex items-center justify-center gap-2 ${isLoading? 'opacity-60 cursor-not-allowed' : ''}`}
                            type="submit">
                            {isLoading ? (
                                <>
                                    <LoaderCircle className="animate-spin w-5 h-5" />
                                    Signing up...
                                </>
                            ) : (
                                "Signup"
                            )}
                        </button>
                        <p className="text-sm text-slate-800 text-center mt-6">
                            Already have an account?
                            <Link to="/login" className="font-medium text-blue-600 underline hover:text-blue-700 transition-colors">Login</Link>
                        </p>
                    </form>
                </div>
            </div>

        </div>
    )
}

export default Signup;
