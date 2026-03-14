export const addThousandSeparator = (num) => {
    if (num === null || isNaN(num))     return "";

    const numStr = num.toString();
    const parts = numStr.split(".");
    let intPart = parts[0];
    let fractionalPart = parts[1];
    const lastThree = intPart.substring(intPart.length - 3);
    const otherNums = intPart.substring(0, intPart.length - 3);

    if (otherNums !== '') {
        const formattedOtherNums = otherNums.replace(/\B(?=(\d{2})+(?!\d))/g, ',');
        intPart = formattedOtherNums + "," + lastThree;
    } else {
        intPart = lastThree;
    }
    return fractionalPart ? `${intPart}.${fractionalPart}` : intPart;
}

const getDateKey = (date) => {
    const parsedDate = new Date(date);

    if (Number.isNaN(parsedDate.getTime())) {
        return null;
    }

    const year = parsedDate.getFullYear();
    const month = String(parsedDate.getMonth() + 1).padStart(2, "0");
    const day = String(parsedDate.getDate()).padStart(2, "0");

    return `${year}-${month}-${day}`;
};

export const prepareLineChartData = (data) => {
    if (!Array.isArray(data) || data.length === 0) {
        return [];
    }

    const dailyTotals = data.reduce((accumulator, income) => {
        const amount = Number(income?.amount);
        const dateKey = getDateKey(income?.date);

        if (!Number.isFinite(amount) || dateKey === null) {
            return accumulator;
        }

        if (!accumulator[dateKey]) {
            accumulator[dateKey] = {
                date: dateKey,
                amount: 0,
                item: []
            };
        }

        accumulator[dateKey].amount += amount;
        accumulator[dateKey].item.push(income);
        return accumulator;
    }, {});

    return Object.keys(dailyTotals)
        .sort((firstDate, secondDate) => firstDate.localeCompare(secondDate))
        .map((dateKey) => dailyTotals[dateKey]);
};
