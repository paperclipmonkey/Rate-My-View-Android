import android.support.test.InstrumentationRegistry;

public class MyEspressoTest
        extends ActivityInstrumentationTestCase2<MyActivity> {

    private MyActivity mActivity;

    public MyEspressoTest() {
        super(MyActivity.class);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
        mActivity = getActivity();
    }

   ...
}