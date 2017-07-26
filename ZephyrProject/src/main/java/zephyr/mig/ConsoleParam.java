package zephyr.mig;

// 프로그램 실행 시 넘어오는 파라미터를 설정. 현재는 삭제여부와 eml 파일경로
public class ConsoleParam {
    private boolean deleteFlag = false;
    private String path;

    public ConsoleParam() {
    }

    public ConsoleParam(String[] args) {
        loadParam(args);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isDeleteFlag() {
        return deleteFlag;
    }

    public void setDeleteFlag(boolean deleteFlag) {
        this.deleteFlag = deleteFlag;
    }

    // console 실행 시 파라미터가 있을 경우 해당하는 설정을 저장
    public void loadParam(String[] args) {
        // -d : 파일을 db에 저장한 후 삭제처리.
        // 경로 : 환경설정 외에 경로를 직접 설정
        if (args.length > 0) {
            for (String arg : args) {
                if ("-d".equals(arg)) {
                    setDeleteFlag(true);
                } else {
                    setPath(arg);
                }
            }
        }
    }

}
