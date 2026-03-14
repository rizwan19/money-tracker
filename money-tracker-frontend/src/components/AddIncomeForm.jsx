import {useEffect, useState} from "react";
import EmojiPickerPopup from "./EmojiPickerPopup.jsx";
import Input from "./input.jsx";
import {LoaderCircle} from "lucide-react";

const AddIncomeForm = ({onAddIncome, categories}) => {
    const [loading, setLoading] = useState(false);
    const [income, setIncome] = useState({
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
        setIncome({...income, [key]: value});
    }

    const handleAddIncome = async (income) => {
        setLoading(true);
        try {
            await onAddIncome(income);
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        if (categories.length > 0 && !income.categoryId) {
            setIncome((prev) => ({...prev, categoryId: categories[0].id}))
        }
    }, [categories, income.categoryId]);

    return (
        <div className="">
            <EmojiPickerPopup
                icon={income.icon}
                onSelect={(selectedIcon) => handleChange('icon', selectedIcon)}
            />
            <Input
                value={income.name}
                onchange={({target}) => handleChange('name', target.value)}
                label="Income Source"
                placeholder="e.g., Salary, Freelance, Bonus etc"
                type="text"
            />
            <Input
                label="Category"
                value={income.categoryId}
                onchange={({target}) => handleChange('categoryId', target.value)}
                isSelect={true}
                options={categoryOptions}
            />
            <Input
                value={income.amount}
                onchange={({target}) => handleChange('amount', target.value)}
                label="Amount"
                placeholder="e.g., 500.00"
                type="number"
            />
            <Input
                value={income.date}
                label="Date"
                onchange={({target}) => handleChange('date', target.value)}
                placeholder=""
                type="date"
            />
            <div className="flex justify-end mt-6">
                <button
                    className="add-btn add-btn-fill"
                    disabled={loading}
                    onClick={() => handleAddIncome(income)}
                    >{loading ? (
                        <>
                            <LoaderCircle className="w-4 h-4 animate-spin" />Adding...
                        </>
                ) : (
                    <>
                        Add Income
                    </>
                )}</button>
            </div>
        </div>
    )
}

export default AddIncomeForm;