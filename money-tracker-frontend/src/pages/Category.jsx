import Dashboard from "../components/Dashboard.jsx";
import {useUser} from "../hooks/useUser.jsx";
import {Plus} from "lucide-react";
import CategoryList from "../components/CategoryList.jsx";
import {useEffect, useState} from "react";
import axiosConfig from "../util/axiosConfig.jsx";
import {API_ENDPOINTS} from "../util/apiEndpoints.js";
import toast from "react-hot-toast";
import Modal from "../components/Modal.jsx";
import AddCategoryForm from "../components/AddCategoryForm.jsx";

const Category = () => {
    useUser();
    const [loading, setLoading] = useState(false);
    const [categoryData, setCategoryData] = useState([]);
    const [openAddCategoryModal, setOpenAddCategoryModal] = useState(false);
    const [openEditCategoryModal, setOpenEditCategoryModal] = useState(false);
    const [selectedCategory, setSelectedCategory] = useState(null);

    const fetchCategoryDetails = async() => {
        if (loading)
            return;
        setLoading(true);

        try{
            const response = await axiosConfig.get(API_ENDPOINTS.GET_ALL_CATEGORIES);

            if (response.status === 200) {
                setCategoryData(response.data);
            }
        } catch (error) {
            toast.error(error.message);
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        fetchCategoryDetails()
    }, []);

    const handeEditCategory = (category) => {
        setSelectedCategory(category);
        setOpenEditCategoryModal(true);
    }

    const handleAddCategory = async (category) => {
        setLoading(true);
        const {name, type, icon} = category;

        if (!name.trim()) {
            toast.error("Category name is required");
            setLoading(false);
            return;
        }

        try {
            const response = await axiosConfig.post(API_ENDPOINTS.ADD_CATEGORY, {name, type, icon});

            if (response.status === 201) {
                toast.success("Category added successfully");
                setOpenAddCategoryModal(false);
                fetchCategoryDetails();
            }
        } catch (error) {
            toast.error(error.response?.data?.message || "Failed to add category");
            setLoading(false);
        } finally {
            setLoading(false);
        }
    }

    const handleUpdateCategory = async (category) => {
        setLoading(true);
        const {id, name, type, icon} = category;

        if (!name.trim()) {
            toast.error("Category name is required");
            setLoading(false);
            return;
        }
        if (!id) {
            toast.error("Category id is missing");
            setLoading(false);
            return;
        }
        try {
            await axiosConfig.put(API_ENDPOINTS.UPDATE_CATEGORY(id), {name, type, icon});
            setOpenEditCategoryModal(false);
            setSelectedCategory(null);
            toast.success("Category added successfully");
            fetchCategoryDetails();
        } catch (error) {
            toast.error(error.response?.data?.message || "Failed to update category");
            setLoading(false);
        } finally {
            setLoading(false);
        }
    }

    return (
        <Dashboard activeMenu="Category">
            <div className="my-5 mx-auto">
                {/* add button */}
                <div className="flex justify-between items-center mb-5">
                    <h2 className="text-2xl font-semibold">All Categories</h2>
                    <button
                        onClick={() => setOpenAddCategoryModal(true)}
                        className="add-btn flex items-center gap-1 cursor-pointer">
                        <Plus size={15} />
                        Add Category
                    </button>
                </div>

                {/*category list */}
                <CategoryList categories={categoryData} onEditCategory={handeEditCategory} />

                {/* adding category to modal */}
                <Modal
                    isOpen={openAddCategoryModal}
                    onClose={() => setOpenAddCategoryModal(false)}
                    title="Add Category"
                ><AddCategoryForm onAddCategory={handleAddCategory} /></Modal>

                {/* edit category to modal */}
                <Modal
                    isOpen={openEditCategoryModal}
                    onClose={() => {setOpenEditCategoryModal(false)
                                setSelectedCategory(null)}}
                    title="Edit Category"
                >
                    <AddCategoryForm
                        initialCategoryData={selectedCategory}
                        onAddCategory={handleUpdateCategory}
                        isEditing={true}
                    />
                </Modal>
            </div>
        </Dashboard>
    )
}

export default Category;