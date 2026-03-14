import Dashboard from "../components/Dashboard.jsx";
import {useUser} from "../hooks/useUser.jsx";
import {Search} from "lucide-react";
import {useState} from "react";
import toast from "react-hot-toast";
import axiosConfig from "../util/axiosConfig.jsx";
import {API_ENDPOINTS} from "../util/apiEndpoints.js";
import TransactionInfoCard from "../components/TransactionInfoCard.jsx";
import moment from "moment";

const Filter = () => {
    useUser();
    const [type, setType] = useState("income");
    const [startDate, setStartDate] = useState("");
    const [endDate, setEndDate] = useState("");
    const [keyword, setKeyword] = useState("");
    const [sortField, setSortField] = useState("date");
    const [sortOrder, setSortOrder] = useState("desc");
    const [transaction, setTransaction] = useState([]);
    const [loading, setLoading] = useState(false);

    const handleSearch = async (e) => {
        e.preventDefault();
        setLoading(true)

        try {
            const response = await axiosConfig.post(API_ENDPOINTS.APPLY_FILTERS, {
                type,
                startDate,
                endDate,
                term: keyword,
                sortField,
                order: sortOrder
            });
            setTransaction(response.data);
        } catch (error) {
            setLoading(false);
            toast.error(error.response?.data?.message || "Failed to filter");
        } finally {
            setLoading(false);
        }
    }

    return (
        <Dashboard activeMenu="Filter">
            <div className="my-5 mx-auto">
                <div className="flex justify-between items-center mb-4">
                    <h2 className="text-2xl font-semibold">Filter Transactions</h2>
                </div>
                <div className="card p-4 mb-4">
                    <div className="flex items-center justify-between mb-4">
                        <h5 className="text-lg font-semibold">Select the filters</h5>
                    </div>
                    <form className="grid grid-cols-1 sm:grid-cols-3 md:grid-cols-6 gap-4">
                        <div>
                            <label className="block text-sm font-medium mb-1" htmlFor="type">Type</label>
                            <select
                                onChange={(e) => setType(e.target.value)}
                                value={type} id="type" className="w-full border rounded px-3 py-2">
                                <option value="income">Income</option>
                                <option value="expense">Expense</option>
                            </select>
                        </div>
                        <div>
                            <label className="block text-sm font-medium mb-1" htmlFor="startDate">Start Date</label>
                            <input
                                onChange={(e) => setStartDate(e.target.value)}
                                value={startDate} type="date" className="w-full border rounded px-3 py-2" />
                        </div>
                        <div>
                            <label className="block text-sm font-medium mb-1" htmlFor="endDate">End Date</label>
                            <input
                                onChange={(e) => setEndDate(e.target.value)}
                                value={endDate} type="date" className="w-full border rounded px-3 py-2" />
                        </div>
                        <div>
                            <label className="block text-sm font-medium mb-1" htmlFor="sortorder">Sort Field</label>
                            <select
                                onChange={(e) => setSortField(e.target.value)}
                                value={sortField} id="sortorder" className="w-full border rounded px-3 py-2">
                                <option value="date">Date</option>
                                <option value="amount">Amount</option>
                                <option value="category">Category</option>
                            </select>
                        </div>
                        <div>
                            <label className="block text-sm font-medium mb-1" htmlFor="sortorder">Sort Order</label>
                            <select
                                onChange={(e) => setSortOrder(e.target.value)}
                                value={sortOrder} id="sortorder" className="w-full border rounded px-3 py-2">
                                <option value="asc">Ascending</option>
                                <option value="desc">Descending</option>
                            </select>
                        </div>
                        <div className="sm:col-span-1 md:col-span-2 flex items-end">
                            <div className="w-full">
                                <label htmlFor="keyword" className="block text-sm font-medium mb-1">Search</label>
                                <input
                                    onChange={(e) => setKeyword(e.target.value)}
                                    value={keyword} className="w-full border rounded px-3 py-2" placeholder="search..." />
                            </div>
                            <button
                                onClick={handleSearch}
                                className="ml-2 mb-1 p-2 bg-purple-800 hover:bg-purple-800 text-white rounded flex items-center justify-center cursor-pointer">
                                <Search size={20} />
                            </button>
                        </div>
                    </form>
                </div>
                <div className="card p-4">
                    <div className="flex justify-between items-center mb-4">
                        <h5 className="text-2xl font-semibold">Transactions</h5>
                    </div>
                    {transaction.length === 0 && !loading ? (
                        <p className="text-gray-500">
                            Select the filters and click search to filter the transactions
                        </p>
                    ) : ""}
                    {loading ? (
                        <p className="text-gray-500"> Loading Transactions ...</p>
                    ) : ("")}
                    {transaction.map((transaction) => (
                        <TransactionInfoCard
                            key={transaction.id}
                            title={transaction.name}
                            icon={transaction.icon}
                            date={moment(transaction.date).format('Do MMM YYYY')}
                            type={type}
                            amount={transaction.amount}
                            hideDeleteButton={true}
                        />
                    ))}
                </div>
            </div>
        </Dashboard>
    )
}

export default Filter;
