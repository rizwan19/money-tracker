import {Cell, Label, Pie, PieChart as RechartsPieChart, ResponsiveContainer, Tooltip} from "recharts";
import {addThousandSeparator} from "../util/util.js";

const formatAmount = (value) => {
    const amount = Number(value);

    if (!Number.isFinite(amount)) {
        return "$0";
    }

    return `$${addThousandSeparator(amount)}`;
};

const renderCenterText = ({viewBox, label, totalAmount, showTextAnchor}) => {
    if (!showTextAnchor || !viewBox) {
        return null;
    }

    const {cx, cy} = viewBox;

    return (
        <text x={cx} y={cy} textAnchor="middle" dominantBaseline="middle">
            <tspan x={cx} dy="-0.6em" fill="#94a3b8" fontSize="12">
                {label}
            </tspan>
            <tspan x={cx} dy="1.6em" fill="#0f172a" fontSize="20" fontWeight="600">
                {totalAmount}
            </tspan>
        </text>
    );
};

const CustomPieChart = ({
    data = [],
    label = "",
    totalAmount = "",
    colors = [],
    showTextAnchor = false
}) => {
    const chartData = data
        .map((item) => ({
            ...item,
            amount: Math.abs(Number(item?.amount) || 0)
        }))
        .filter((item) => item.amount > 0);

    const fallbackData = chartData.length > 0 ? chartData : [{name: "No Data", amount: 1}];

    return (
        <div className="mt-8">
            <div className="h-72">
                <ResponsiveContainer width="100%" height="100%">
                    <RechartsPieChart>
                        <Pie
                            data={fallbackData}
                            dataKey="amount"
                            nameKey="name"
                            cx="50%"
                            cy="50%"
                            innerRadius={70}
                            outerRadius={105}
                            paddingAngle={4}
                            strokeWidth={0}
                            labelLine={false}
                            isAnimationActive={false}
                        >
                            {fallbackData.map((entry, index) => (
                                <Cell
                                    key={`${entry.name}-${index}`}
                                    fill={colors[index % colors.length] || "#cbd5e1"}
                                />
                            ))}
                            {showTextAnchor && (
                                <Label
                                    position="center"
                                    content={(value) => renderCenterText({
                                        ...value,
                                        label,
                                        totalAmount,
                                        showTextAnchor
                                    })}
                                />
                            )}
                        </Pie>
                        <Tooltip
                            formatter={(value, _name, item) => [
                                formatAmount(value),
                                item?.payload?.name || "Amount"
                            ]}
                        />
                    </RechartsPieChart>
                </ResponsiveContainer>
            </div>

            <div className="mt-4 grid gap-3 sm:grid-cols-3">
                {data.map((item, index) => (
                    <div
                        key={`${item?.name || "item"}-${index}`}
                        className="rounded-xl border border-slate-200 bg-slate-50 px-3 py-2"
                    >
                        <div className="flex items-center gap-2">
                            <span
                                className="h-2.5 w-2.5 rounded-full"
                                style={{backgroundColor: colors[index % colors.length] || "#cbd5e1"}}
                            />
                            <span className="text-xs text-slate-500">{item?.name}</span>
                        </div>
                        <p className="mt-2 text-sm font-semibold text-slate-900">
                            {formatAmount(item?.amount)}
                        </p>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default CustomPieChart;
