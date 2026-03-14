import Dashboard from "../components/Dashboard.jsx";
import {useUser} from "../hooks/useUser.jsx";
import {useEffect, useState} from "react";
import toast from "react-hot-toast";
import axiosConfig from "../util/axiosConfig.jsx";
import {API_ENDPOINTS} from "../util/apiEndpoints.js";
import ExpenseList from "../components/ExpenseList.jsx";
import Modal from "../components/Modal.jsx";
import DeleteAlert from "../components/DeleteAlert.jsx";
import Overview from "../components/Overview.jsx";
import AddExpenseForm from "../components/AddExpenseForm.jsx";

const Expense = () => {
    const expenseMessage = "Track your expenses over time and analyze your expense trends.";
    useUser();
    const [expenseData, setExpenseData] = useState([]);
    const [categories, setCategories] = useState([]);
    const [loading, setLoading] = useState(false);
    const [openAddExpenseModal, setOpenAddExpenseModal] = useState(false);
    const [openDeleteAlert, setOpenDeleteAlert] = useState({
        show: false,
        data: null
    })
    const fetchExpenseDetails = async () => {
        if (loading)
            return;
        setLoading(true);

        try {
            const response = await axiosConfig.get(API_ENDPOINTS.GET_ALL_EXPENSE);
            if (response.status === 200)
                setExpenseData(response.data);
        } catch (error) {
            toast.error(error.response?.data?.message || "Failed to load expense details");
            setLoading(false);
        } finally {
            setLoading(false);
        }
    }

    const fetchExpenseCategories = async () => {
        try {
            const response = await axiosConfig.get(API_ENDPOINTS.CATEGORY_BY_TYPE("expense"));

            if (response.status === 200) {
                setCategories(response.data);
            }
        } catch (error) {
            toast.error(error.response?.data?.message || "Failed to fetch expense categories");
        }
    }

    const handleAddExpense = async (expense) => {
        setLoading(true);
        const {name, amount, date, icon, categoryId} = expense;

        if (!name.trim()) {
            toast.error("Please enter a name");
            setLoading(false);
            return;
        }
        if (!amount || isNaN(amount) || Number(amount)<=0) {
            toast.error("Amount should be a valid number");
            setLoading(false);
            return;
        }
        if (!date) {
            toast.error("Date is required");
            setLoading(false);
            return;
        }
        const today = new Date().toISOString().split('T')[0];
        if (date > today) {
            toast.error("Date cannot be in the future");
            setLoading(false);
            return;
        }
        if (!categoryId) {
            console.log(categoryId);
            toast.error("Category is required");
            setLoading(false);
            return;
        }

        try {
            const response = await axiosConfig.post(API_ENDPOINTS.ADD_EXPENSE, {
                name,
                amount: Number(amount),
                date,
                icon,
                categoryId
            });

            if (response.status === 201) {
                setOpenAddExpenseModal(false);
                toast.success("Expense added successfully");
                fetchExpenseDetails();
                fetchExpenseCategories();
            }
        } catch (error) {
            toast.error(error.response?.data?.message || "Failed to add expense");
            setLoading(false);
        } finally {
            setLoading(false);
        }
    }

    const deleteExpense = async (id) => {
        try {
            await axiosConfig.delete(API_ENDPOINTS.DELETE_EXPENSE(id));
            setOpenDeleteAlert({show: false, data: null});
            toast.success("Expense deleted successfully");
            fetchExpenseDetails();
        } catch (error) {
            toast.error(error.response?.data?.message || "Failed to delete expense");
        }
    }

    const handleDownloadExpenseDetails = async () => {
        try {
            const response = await axiosConfig.get(API_ENDPOINTS.EXPENSE_EXCEL_DOWNLOAD, {responseType: "blob"});
            let fileName = "expense_details.xlsx";
            const url = window.URL.createObjectURL(new Blob([response.data]));
            const link = document.createElement("a");
            link.href = url;
            link.setAttribute("download", fileName);
            document.body.appendChild(link);
            link.click();
            link.parentNode.removeChild(link);
            window.URL.revokeObjectURL(url);
            toast.success("Expense details downloaded successfully");
        } catch (error) {
            toast.error(error.response?.data?.message || "Failed to download expense details");
        }
    }
    const handleEmailExpenseDetails = async () => {
        try {
            const response = await axiosConfig.get(API_ENDPOINTS.EMAIL_EXPENSE);
            if (response.status === 200)
                toast.success("Expense details emailed successfully");
        } catch (error) {
            toast.error(error.response?.data?.message || "Failed to email the expense details");
        }
    }

    useEffect(() => {
        fetchExpenseDetails();
        fetchExpenseCategories();
    }, []);

    return (
        <Dashboard activeMenu="Expense">
            <div className="my-5 mx-auto">
                <div className="grid grid-cols-1 gap-6">
                    <div>
                        {/* overview */}
                        <Overview
                            transactions={expenseData}
                            message={expenseMessage}
                            onAdd={() => setOpenAddExpenseModal(true)} />
                    </div>
                    <ExpenseList transactions={expenseData}
                                onDownload={handleDownloadExpenseDetails}
                                onEmail={handleEmailExpenseDetails}
                                onDelete={(id) => setOpenDeleteAlert({show: true, data: id})}
                    />

                    {/* Add Expense Modal */}
                    <Modal
                        isOpen={openAddExpenseModal}
                        onClose={() => setOpenAddExpenseModal(false)}
                        title="Add Expense"
                    >
                        <AddExpenseForm onAddExpense={(expense) => handleAddExpense(expense)} categories={categories} />
                    </Modal>

                    {/* Delete expense modal */}
                    <Modal
                        isOpen={openDeleteAlert.show}
                        onClose={() => setOpenDeleteAlert({show: false, data: null})}
                        title="Delete Expense"
                    >
                        <DeleteAlert
                            content="Are you sure you want to delete the expense?"
                            onDelete={() => deleteExpense(openDeleteAlert.data)}
                        />
                    </Modal>
                </div>
            </div>
        </Dashboard>
    )
}

export default Expense;