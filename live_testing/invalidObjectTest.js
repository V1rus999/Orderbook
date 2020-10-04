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
      objectToPost,
      {
        headers: {
          "api-key": "123",
        },
      }
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
  const sellRes = await addLimitOrder(invalidObject);
  console.log(sellRes.data);
  const buyRes = await addLimitOrder(buy);
  console.log(buyRes.data);
  const trades = await doTradesListRequest();
  console.log("Done");
}

main().then(console.log(""));
