const axios = require("axios").default;

const sell = {
  side: "SELL",
  quantity: "0.100000",
  price: "10002",
  pair: "BTCZAR",
  postOnly: true,
  customerOrderId: "1235",
};

const buy = {
  side: "BUY",
  quantity: "0.100000",
  price: "10002",
  pair: "BTCZAR",
  postOnly: true,
  customerOrderId: "1235",
}

async function addLimitOrder(objectToPost) {
  try {
    const response = await axios.post(
      "http://localhost:8080/orders/limit",
      objectToPost
    );
    return response;
  } catch (error) {
    console.error("addLimitOrder");
    console.error(error);
    return null;
  }
}

async function doTradesListRequest() {
  try {
    const response = await axios.get("http://localhost:8080/orders/trades");
    return response;
  } catch (error) {
    console.error("doTradesListRequest");
    console.error(error);
    return null;
  }
}

async function main() {
  console.log("Starting");
  const sellRes = await addLimitOrder(sell);
  console.log(sellRes.status);
  const buyRes = await addLimitOrder(buy);
  console.log(buyRes.status);
  const trades = await doTradesListRequest();
  console.log("Done");
}

main().then(console.log(""));
