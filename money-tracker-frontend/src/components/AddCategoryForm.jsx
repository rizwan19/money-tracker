import {useEffect, useState} from "react";
import Input from "./input.jsx";
import EmojiPickerPopup from "./EmojiPickerPopup.jsx";
import {LoaderCircle} from "lucide-react";

const AddCategoryForm = ({onAddCategory, initialCategoryData, isEditing}) => {
    const [category, setCategory] = useState({
        name: "",
        type: "income",
        icon: ""
    });

    useEffect(() => {
        if (isEditing && initialCategoryData) {
            setCategory(initialCategoryData);
        }
        else {
            setCategory({name: "", type: "income", icon: ""});
        }
    }, [isEditing, initialCategoryData]);

    const [loading, setLoading] = useState(false);
    const categoryTypes = [
        {value: "income", label: "Income"},
        {value: "expense", label: "Expense"}
    ]

    const handleChange = (key, value) => {
        setCategory({...category, [key]: value});
    }

    const handleSubmit = async () => {
        setLoading(true);
        try {
            await onAddCategory(category);
        } finally {
            setLoading(false);
        }
    }

    return (
        <div className="p-4">
            <EmojiPickerPopup
                icon={category.icon}
                onSelect={(selectedIcon) => handleChange("icon", selectedIcon)}
            />
            <Input
                value={category.name}
                onchange={({target}) => handleChange("name", target.value)}
                label="Category Name"
                placeholder="e.g., Freelance, Salary, Groceries"
                type="text" />
            <Input
                label="Category Type"
                value={category.type}
                onchange={({target}) => handleChange("type", target.value)}
                isSelect={true}
                options={categoryTypes}
            />
            <div className="flex justify-end mt-6">
                <button
                    type="button"
                    disabled={loading}
                    onClick={handleSubmit}
                    className="add-btn add-btn-fill cursor-pointer">
                    {loading ? (
                        <>
                            <LoaderCircle className="w-4 h-4 animate-spin" />
                            {isEditing ? "Updating..." : "Adding..."}
                        </>
                    ) : (
                        <>{isEditing ? "Update Category" : "Add Category"}</>
                    )}
                </button>
            </div>
        </div>
    )
}

export default AddCategoryForm;