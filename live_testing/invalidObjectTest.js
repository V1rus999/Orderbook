const axios = require("axios").default;

const invalidObject = {
  name: "Foo",
  surname: "Bar",
};

const emptyObj = {};

async function addLimitOrder(objectToPost) {
  try {
    const response = await axios.post(
      "http://localhost:8080/orders/limit",
      invalidObject
    );
    return response;
  } catch (error) {
    console.error("addLimitOrder");
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
