const axios = require("axios").default;

function createBuy(quantity, price) {
  return {
    side: "BUY",
    quantity: quantity,
    price: price,
    pair: "BTCZAR",
    postOnly: true,
    customerOrderId: "1235",
  };
}

function createSell(quantity, price) {
  return {
    side: "SELL",
    quantity: quantity,
    price: price,
    pair: "BTCZAR",
    postOnly: true,
    customerOrderId: "1235",
  };
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

async function checkTimings() {
  try {
    const response = await axios.get("http://localhost:8080/orders/timings");
    return response;
  } catch (error) {
    console.error("doTradesListRequest");
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

async function doABunchOfTrades() {
  for (var i = 0; i < 100; i++) {
    await addLimitOrder(createBuy(0.3, 4000));
    await addLimitOrder(createBuy(0.3, 3000));
    await addLimitOrder(createBuy(0.3, 2000));
    await addLimitOrder(createSell(1, 1500));
  }
}

async function main() {
  const resp = await addLimitOrder(createBuy(0.4, 4000));
  // await doABunchOfTrades();
  // await doABunchOfTrades();
  // await doABunchOfTrades();
  // await doABunchOfTrades();
  // const res = await checkTimings();
  console.log(resp.data);
  console.log("Done");
}

main().then(console.log(""));
