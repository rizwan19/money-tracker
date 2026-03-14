import Dashboard from "../components/Dashboard.jsx";
import {useUser} from "../hooks/useUser.jsx";
import InfoCard from "../components/InfoCard.jsx";
import {Coins, Wallet, WalletCards} from "lucide-react";
import {addThousandSeparator} from "../util/util.js";
import {useNavigate} from "react-router-dom";
import {useEffect, useState} from "react";
import toast from "react-hot-toast";
import axiosConfig from "../util/axiosConfig.jsx";
import {API_ENDPOINTS} from "../util/apiEndpoints.js";
import RecentTransactions from "../components/RecentTransactions.jsx";
import FinanceOverview from "../components/FinanceOverview.jsx";
import Transactions from "../components/Transactions.jsx";

const Home = () => {
    useUser();
    const navigate = useNavigate();
    const [dashboardData, setDashboardData] = useState(null);
    const [loading, setLoading] = useState(false);

    const fetchDashboardData = async () => {
        if (loading) return;

        setLoading(true);
        try {
            const response = await axiosConfig.get(API_ENDPOINTS.DASHBOARD_DATA);

            if (response.status === 200)
                setDashboardData(response.data);

        } catch (error) {
            toast.error(error.response?.data?.message || "Failed to fetch dashboard data");
        } finally {
            setLoading(false);
        }
    }

    useEffect( () => {
        fetchDashboardData();
        return () => {};
    }, []);

    return (
        <div>
            <Dashboard activeMenu="Dashboard">
                <div className="my-5 mx-auto">
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                        {/* display the cards */}
                        <InfoCard
                            icon={<WalletCards />}
                            label="Total Balance"
                            value={addThousandSeparator(dashboardData?.totalBalance || 0)}
                            color="bg-purple-800"
                        />
                        <InfoCard
                            icon={<Wallet />}
                            label="Total Income"
                            value={addThousandSeparator(dashboardData?.totalIncome || 0)}
                            color="bg-purple-800"
                        />
                        <InfoCard
                            icon={<Coins />}
                            label="Total Expense"
                            value={addThousandSeparator(dashboardData?.totalExpense || 0)}
                            color="bg-purple-800"
                        />
                    </div>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mt-6">
                        {/* Recent Transactions */}
                        <RecentTransactions
                            transactions={dashboardData?.recentTransactions}
                            onMore={() => navigate("/expense")}
                        />

                        {/* Finance overview chart */}
                        <FinanceOverview
                            totalBalance={dashboardData?.totalBalance || 0}
                            totalIncome={dashboardData?.totalIncome || 0}
                            totalExpense={dashboardData?.totalExpense || 0}
                        />

                        {/* Expense transactions */}
                        <Transactions
                            transactions={dashboardData?.recentFiveExpenses || []}
                            onMore={() => navigate("/expense")}
                            type="expense"
                            title="Recent Expenses"
                        />

                        {/* Income transactions */}
                        <Transactions
                            transactions={dashboardData?.recentFiveIncomes || []}
                            onMore={() => navigate("/income")}
                            type="income"
                            title="Recent Incomes"
                        />
                    </div>
                </div>
            </Dashboard>
        </div>
    )
}

export default Home;
