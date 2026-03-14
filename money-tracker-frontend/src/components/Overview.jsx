import {prepareLineChartData} from "../util/util.js";
import {
    CartesianGrid,
    Line,
    LineChart,
    ResponsiveContainer,
    Tooltip,
    XAxis,
    YAxis
} from "recharts";
import {Plus} from "lucide-react";

const formatChartDate = (value) => {
    const parsedDate = new Date(value);

    if (Number.isNaN(parsedDate.getTime())) {
        return value;
    }

    return parsedDate.toLocaleDateString("en-US", {
        month: "short",
        day: "numeric",
        year: "numeric"
    });
};

const formatChartAmount = (value) => {
    const amount = Number(value);

    if (!Number.isFinite(amount)) {
        return "";
    }

    return new Intl.NumberFormat("en-US", {
        maximumFractionDigits: 0
    }).format(amount);
};

const Overview = ({transactions, onAdd, message}) => {
    const chartData = prepareLineChartData(transactions);

    return (
        <div className="card">
            <div className="flex items-center justify-between">
                <div>
                    <h5 className="text-lg">
                        Overview
                    </h5>
                    <p className="text-xs text-gray-400 mt-1">
                        {message}
                    </p>
                </div>
                <button
                    onClick={onAdd}
                    className="add-btn">
                    <Plus size={15} className="text-lg" />Add
                </button>
            </div>
            <div className="mt-10 h-72">
                {/* line chart */}
                <ResponsiveContainer width="100%" height="100%">
                    <LineChart data={chartData}>
                        <CartesianGrid strokeDasharray="3 3" />
                        <XAxis dataKey="date" tickFormatter={formatChartDate} />
                        <YAxis
                            width={90}
                            tickMargin={8}
                            tickFormatter={formatChartAmount}
                        />
                        <Tooltip
                            labelFormatter={formatChartDate}
                            formatter={(value) => [formatChartAmount(value), "Amount"]}
                        />
                        <Line
                            type="monotone"
                            dataKey="amount"
                            stroke="#2563eb"
                            strokeWidth={3}
                            dot={{r: 4}}
                            activeDot={{r: 6}}
                        />
                    </LineChart>
                </ResponsiveContainer>
            </div>
        </div>
    )
}

export default Overview;
