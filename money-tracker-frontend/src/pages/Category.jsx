import Dashboard from "../components/Dashboard.jsx";
import {useUser} from "../hooks/useUser.jsx";

const Category = () => {
    useUser();
    return (
        <Dashboard activeMenu="Category">
            this is category page
        </Dashboard>
    )
}

export default Category;