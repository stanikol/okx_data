import okx.Finance.Savings as Savings

# API initialization
apikey = "c471d095-b6a5-47a8-9bc1-36fd013ee27d"
secretkey = "8ABD38CC8BCF10C4901A52CB169CB196"
passphrase = "Sator!23"
BASE_URL = 'https://aws.okex.com'

flag = "0"  # Production trading:0 , demo trading:1

SavingsAPI = Savings.SavingsAPI(apikey, secretkey, passphrase, False, flag)

result = SavingsAPI.get_saving_balance(ccy="USDT")
print(result)
