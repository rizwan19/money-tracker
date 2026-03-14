import {useEffect, useState} from "react";
import EmojiPickerPopup from "./EmojiPickerPopup.jsx";
import Input from "./input.jsx";
import {LoaderCircle} from "lucide-react";

const AddExpenseForm = ({onAddExpense, categories}) => {
    const [loading, setLoading] = useState(false);
    const [expense, setExpense] = useState({
        name: '',
        amount: '',
        date: '',
        icon: '',
        categoryId: ''
    })

    const categoryOptions = categories.map(category => ({
        value: category.id,
        label: category.name
    }))

    const handleChange = (key, value) => {
        setExpense({...expense, [key]: value});
    }

    const handleAddExpense = async (expense) => {
        setLoading(true);
        try {
            await onAddExpense(expense);
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        if (categories.length > 0 && !expense.categoryId) {
            setExpense((prev) => ({...prev, categoryId: categories[0].id}))
        }
    }, [categories, expense.categoryId]);

    return (
        <div className="">
            <EmojiPickerPopup
                icon={expense.icon}
                onSelect={(selectedIcon) => handleChange('icon', selectedIcon)}
            />
            <Input
                value={expense.name}
                onchange={({target}) => handleChange('name', target.value)}
                label="Expense Source"
                placeholder="e.g., Bills, Grocery etc"
                type="text"
            />
            <Input
                label="Category"
                value={expense.categoryId}
                onchange={({target}) => handleChange('categoryId', target.value)}
                isSelect={true}
                options={categoryOptions}
            />
            <Input
                value={expense.amount}
                onchange={({target}) => handleChange('amount', target.value)}
                label="Amount"
                placeholder="e.g., 500.00"
                type="number"
            />
            <Input
                value={expense.date}
                label="Date"
                onchange={({target}) => handleChange('date', target.value)}
                placeholder=""
                type="date"
            />
            <div className="flex justify-end mt-6">
                <button
                    className="add-btn add-btn-fill"
                    disabled={loading}
                    onClick={() => handleAddExpense(expense)}
                    >{loading ? (
                        <>
                            <LoaderCircle className="w-4 h-4 animate-spin" />Adding...
                        </>
                ) : (
                    <>
                        Add Expense
                    </>
                )}</button>
            </div>
        </div>
    )
}

export default AddExpenseForm;