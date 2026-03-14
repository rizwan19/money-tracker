import {Download, LoaderCircle, Mail} from "lucide-react";
import TransactionInfoCard from "./TransactionInfoCard.jsx";
import moment from "moment";
import {useState} from "react";

const ExpenseList = ({transactions, onDelete, onDownload, onEmail}) => {

    const [loading, setLoading] = useState(false);

    const handleDownload = async () => {
        setLoading(true);
        try {
            await onDownload();
        } finally {
            setLoading(false);
        }
    }
    const handleEmail = async () => {
        setLoading(true);
        try {
            await onEmail();
        } finally {
            setLoading(false);
        }
    }

    return (
        <div className="card">
            <div className="flex items-center justify-between mb-4">
                <h5 className="text-lg font-semibold">Expense Sources</h5>
                <div className="flex items-center justify-end gap-2">
                    <button disabled={loading} className="card-btn" onClick={handleEmail}>
                        {loading ? (
                            <>
                                <LoaderCircle className="w-4 h-4 animate-spin" />
                                Emailing...
                            </>
                        ) : (
                            <>
                                <Mail size={15} className="text-base" /> Email
                            </>
                        )}
                    </button>
                    <button disabled={loading} className="card-btn" onClick={handleDownload}>
                        {loading ? (
                            <>
                                <LoaderCircle className="w-4 h-4 animate-spin" /> Downloading...
                            </>
                        ) : (
                            <>
                                <Download size={15} className="text-base" /> Download
                            </>
                        )}
                    </button>
                </div>
            </div>
            <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-6">
                {transactions?.map((expense) => (
                    <TransactionInfoCard
                        key={expense.id}
                        title={expense.name}
                        icon={expense.icon}
                        date={moment(expense.date).format('Do MMM YYYY')}
                        amount={expense.amount}
                        type="expense"
                        onDelete={() => onDelete(expense.id)}
                    />
                ))}
            </div>
        </div>
    )
}

export default ExpenseList;
